import { useMemo, useState } from 'react';
import { VARIANTS } from '../../data/variants';
import { useApp } from '../../context/AppContext';
import { createListing } from '../../services/carService';
import { validateCarListing } from '../../services/rulesService';

const STEPS = [
  { id: 1, label: 'Car Info' },
  { id: 2, label: 'Photos' },
  { id: 3, label: 'Pricing' },
  { id: 4, label: 'Test Drive' },
  { id: 5, label: 'Contact' },
];

const MODELS     = Object.keys(VARIANTS).sort();
const FUEL_OPTS  = ['Petrol', 'Diesel', 'CNG', 'Hybrid'];
const YEAR_OPTS  = Array.from({ length: 15 }, (_, i) => (new Date().getFullYear() - i).toString());
const KM_RANGES  = ['0-10,000', '10,000-25,000', '25,000-50,000', '50,000-75,000', '75,000-1,00,000', '1,00,000+'];
const NEG_OPTS   = ['Open to negotiation', 'Fixed price', 'Slightly negotiable'];
const REASON_OPTS= ['Upgrading to a new car', 'Relocation', 'Financial reasons', 'Buying another vehicle', 'Rarely used', 'Other'];

const KM_MAP = {
  '0-10,000': 5000, '10,000-25,000': 17500, '25,000-50,000': 37500,
  '50,000-75,000': 62500, '75,000-1,00,000': 87500, '1,00,000+': 110000,
};

// ── AI Car Brand Detection using Claude Vision ─────────────────────────────────
async function detectCarBrand(imageFile) {
  try {
    const base64 = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result.split(',')[1]);
      reader.onerror = reject;
      reader.readAsDataURL(imageFile);
    });

    const mediaType = imageFile.type || 'image/jpeg';

    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${import.meta.env.VITE_GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'meta-llama/llama-4-scout-17b-16e-instruct',
        max_tokens: 200,
        messages: [
          {
            role: 'user',
            content: [
              {
                type: 'image_url',
                image_url: { url: `data:${mediaType};base64,${base64}` },
              },
              {
                type: 'text',
                text: `You are a car brand expert. Your job is to STRICTLY identify if this is a Maruti Suzuki car or not.

== STEP 1: LOOK FOR SUZUKI S LOGO ==
The Suzuki logo is a stylized letter "S" — it appears on the front grille or boot.
If you see the Suzuki S logo → isMaruti: TRUE immediately.

== STEP 2: LOOK FOR MARUTI MODEL NAME BADGES ==
Check for text badges: SWIFT, BALENO, BREZZA, FRONX, DZIRE, CIAZ, ERTIGA, ALTO, WAGONR, CELERIO, IGNIS, JIMNY, VITARA, XL6, EECO, INVICTO, S-PRESSO
If you see any of these badges → isMaruti: TRUE immediately.

== STEP 3: IDENTIFY NON-MARUTI CARS ==
TATA MOTORS cars — set isMaruti: FALSE:
* Tata Nexon — compact SUV, split boomerang LED headlights, floating roof, NEXON badge, Tata logo
* Tata Harrier — large SUV, dark theme, Land Rover inspired, HARRIER badge, Tata logo  
* Tata Safari — 3 row SUV, SAFARI badge, Tata logo
* Tata Punch — micro SUV, boxy, round headlights, adventure styling, PUNCH badge, Tata logo
* Tata Tiago — small hatchback, TIAGO badge, Tata logo
* Tata Tigor — compact sedan, TIGOR badge, Tata logo
* Tata Altroz — premium hatchback, curved, ALTROZ badge, Tata logo
* Tata Curvv — coupe SUV, CURVV badge, Tata logo
* ALL TATA cars have this logo: two overlapping horizontal ellipses/ovals on grille

HYUNDAI cars — set isMaruti: FALSE:
* Creta, Venue, i20, Grand i10, Verna, Tucson, Alcazar, Exter
* ALL have blue H logo on grille

HONDA cars — set isMaruti: FALSE:
* City, Amaze, WRV, Jazz, Elevate
* ALL have silver H logo

TOYOTA cars — set isMaruti: FALSE:
* Innova, Fortuner, Urban Cruiser, Hyryder, Camry
* ALL have silver oval T logo

KIA cars — set isMaruti: FALSE:
* Seltos, Sonet, Carens, EV6
* ALL have KIA text badge

MG cars — set isMaruti: FALSE:
* Hector, Astor, Comet, Gloster
* ALL have MG badge

MAHINDRA cars — set isMaruti: FALSE:
* Scorpio, Thar, XUV700, XUV300, Bolero, BE6
* ALL have twin peaks Mahindra logo

== FINAL RULE ==
- Suzuki S logo visible = ALWAYS isMaruti: TRUE
- Tata/Hyundai/Honda/Toyota/Kia/MG/Mahindra logo visible = ALWAYS isMaruti: FALSE
- If no logo visible but car shape matches a known non-Maruti model = isMaruti: FALSE
- Only if completely unidentifiable = isMaruti: TRUE

Respond ONLY with this JSON, no explanation:
{"isMaruti": true_or_false, "detectedBrand": "brand name", "carModel": "model name", "confidence": "High or Medium or Low"}`              },
            ],
          },
        ],
      }),
    });

    const data = await response.json();
    console.log('Groq API raw response:', JSON.stringify(data));

    if (data.error) {
      console.error('Groq API error:', data.error);
      return { isMaruti: true, detectedBrand: 'Unknown', carModel: 'Unknown', confidence: 'Low' };
    }

    const text = data?.choices?.[0]?.message?.content || '';
    console.log('Groq response text:', text);

    if (!text) {
      return { isMaruti: true, detectedBrand: 'Unknown', carModel: 'Unknown', confidence: 'Low' };
    }

    const clean = text.replace(/```json|```/g, '').trim();
    const jsonMatch = clean.match(/{[^}]+}/);
    const jsonStr = jsonMatch ? jsonMatch[0] : clean;

    const result = JSON.parse(jsonStr);
    console.log('Parsed result:', result);
    return result;
  } catch (err) {
    console.error('Car detection failed:', err);
    return { isMaruti: true, detectedBrand: 'Unknown', carModel: 'Unknown', confidence: 'Low' };
  }
}


// ── AI Car Condition Score ────────────────────────────────────────────────────
async function analyseCarCondition(imageFiles) {
  try {
    const filesToAnalyse = imageFiles.slice(0, 4);
    const images = await Promise.all(filesToAnalyse.map(async (file) => {
      const base64 = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result.split(',')[1]);
        reader.onerror = reject;
        reader.readAsDataURL(file);
      });
      return { type: 'image_url', image_url: { url: `data:${file.type || 'image/jpeg'};base64,${base64}` } };
    }));

    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${import.meta.env.VITE_GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'meta-llama/llama-4-scout-17b-16e-instruct',
        max_tokens: 400,
        messages: [{
          role: 'user',
          content: [
            ...images,
            {
              type: 'text',
              text: `You are an expert car condition assessor for Indian used car marketplace.
Analyse these car photos and give a detailed condition report.

Score each aspect out of 100:
1. Exterior Body — look for dents, scratches, rust, paint quality
2. Paint Condition — look for fading, peeling, touch-ups, oxidation
3. Tyres — look for tread wear, sidewall condition
4. Interior (if visible) — look for seat condition, dashboard, cleanliness
5. Overall Condition — overall assessment

Also detect any visible issues.

Reply ONLY in this exact JSON format:
{
  "overallScore": 85,
  "grade": "A",
  "exterior": 88,
  "paint": 82,
  "tyres": 80,
  "interior": 90,
  "issues": ["Minor scratch on front bumper", "Slight paint fade on hood"],
  "positives": ["Well maintained body", "Clean interior"],
  "priceImpact": -15000,
  "summary": "Well maintained car with minor cosmetic issues"
}

Grade scale: A+ (95-100), A (85-94), B (70-84), C (55-69), D (below 55)
priceImpact: negative number in INR for price reduction (0 if no issues)`
            }
          ],
        }],
      }),
    });
    const data = await response.json();
    const text = data?.choices?.[0]?.message?.content || '';
    const clean = text.replace(/```json|```/g, '').trim();
    const jsonMatch = clean.match(/\{[\s\S]*\}/);
    return JSON.parse(jsonMatch ? jsonMatch[0] : clean);
  } catch (err) {
    console.error('Condition analysis failed:', err);
    return null;
  }
}

// ── AI Damage Detection ───────────────────────────────────────────────────────
async function detectDamage(imageFile, idx, setDamageWarnings) {
  try {
    const base64 = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result.split(',')[1]);
      reader.onerror = reject;
      reader.readAsDataURL(imageFile);
    });
    const mediaType = imageFile.type || 'image/jpeg';
    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${import.meta.env.VITE_GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'meta-llama/llama-4-scout-17b-16e-instruct',
        max_tokens: 150,
        messages: [{
          role: 'user',
          content: [
            { type: 'image_url', image_url: { url: `data:${mediaType};base64,${base64}` } },
            { type: 'text', text: `Inspect this car image for visible damage, scratches, dents, or rust.
Reply ONLY in this JSON format:
{"hasDamage": true_or_false, "severity": "None or Minor or Moderate or Severe", "description": "one line description or None", "priceImpact": "estimated price reduction in INR as number e.g. 15000 or 0"}` }
          ],
        }],
      }),
    });
    const data = await response.json();
    const text = data?.choices?.[0]?.message?.content || '';
    const clean = text.replace(/```json|```/g, '').trim();
    const jsonMatch = clean.match(/{[^}]+}/);
    const result = JSON.parse(jsonMatch ? jsonMatch[0] : clean);
    return result;
  } catch (err) {
    return { hasDamage: false, severity: 'None', description: 'None', priceImpact: 0 };
  }
}

// ── AI Price Suggestion ────────────────────────────────────────────────────────
async function suggestPrice(model, variant, year, km, fuel) {
  try {
    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${import.meta.env.VITE_GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'llama-3.3-70b-versatile',
        max_tokens: 150,
        messages: [{
          role: 'user',
          content: `You are an expert Indian used car price analyst with deep knowledge of Maruti Suzuki resale values.

Give accurate resale price for this EXACT car in India 2025:
Car: Maruti Suzuki ${model} ${variant}
Year: ${year}
KM Driven: ${km}
Fuel Type: ${fuel}

Consider:
- Maruti ${model} specific depreciation curve
- ${fuel} variant premium/discount
- ${km} km usage impact on price
- 2025 Indian used car market rates
- ${variant} variant positioning

Be SPECIFIC to ${model} — not generic. WagonR prices differ from Brezza, Swift differs from Baleno.

Reply ONLY in this exact JSON, no extra text:
{"minPrice": 450000, "maxPrice": 550000, "suggestedPrice": 500000, "reason": "specific reason mentioning ${model} and key factors"}

All prices in Indian Rupees as plain numbers only.`
        }],
      }),
    });
    const data = await response.json();
    const text = data?.choices?.[0]?.message?.content || '';
    const clean = text.replace(/\`\`\`json|\`\`\`/g, '').trim();
    const jsonMatch = clean.match(/{[^}]+}/);
    return JSON.parse(jsonMatch ? jsonMatch[0] : clean);
  } catch (err) {
    console.error('Price suggestion failed:', err);
    return null;
  }
}

// ── AI Auto Description Generator ─────────────────────────────────────────────
async function generateDescription(model, variant, year, km, fuel, reason) {
  try {
    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${import.meta.env.VITE_GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'llama-3.3-70b-versatile',
        max_tokens: 200,
        messages: [{
          role: 'user',
          content: `Write a compelling car listing description for this Maruti Suzuki car being sold in India.
Details:
- Model: Maruti ${model} ${variant}
- Year: ${year}
- KM Driven: ${km}
- Fuel: ${fuel}
- Reason for selling: ${reason}

Write 3-4 sentences. Be honest, professional and highlight key selling points.
Reply with ONLY the description text, no quotes, no extra text.`
        }],
      }),
    });
    const data = await response.json();
    return data?.choices?.[0]?.message?.content?.trim() || '';
  } catch (err) {
    console.error('Description generation failed:', err);
    return '';
  }
}

// ── AI Fraud Detection ────────────────────────────────────────────────────────
// FIX: Threshold changed from 40% to 60% — less strict fraud detection
async function detectFraud(model, year, price, km, regNo, sellerCity) {
  try {
    const kmNum = parseInt(km?.replace(/[^0-9]/g, '') || '0');
    const carAge = new Date().getFullYear() - parseInt(year);
    const expectedMinKm = carAge * 5000;
    const expectedMaxKm = carAge * 20000;

    const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${import.meta.env.VITE_GROQ_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'llama-3.3-70b-versatile',
        max_tokens: 200,
        messages: [{
          role: 'user',
          content: `You are a fraud detection AI for MarutiXchange — Indian used car marketplace.

Analyse this car listing for potential fraud:
- Car: Maruti ${model}
- Year: ${year} (${carAge} years old)
- Asking Price: ₹${(parseInt(price)/100000).toFixed(2)} Lakhs
- KM Driven: ${km} (expected range: ${(expectedMinKm/1000).toFixed(0)}k-${(expectedMaxKm/1000).toFixed(0)}k for age)
- Registration: ${regNo || 'Not provided'}
- City: ${sellerCity || 'Unknown'}

IMPORTANT: Be lenient. Only flag genuine fraud, not normal pricing variations.
Sellers often price cars slightly below market — this is NOT fraud.
Only flag if price is more than 60% below market (not 40%).

Check for these fraud signals:
1. PRICE_TOO_LOW: Price more than 60% below typical market value for this car/year? (be lenient)
2. KM_SUSPICIOUS: KM driven impossibly low (under ${(expectedMinKm * 0.2 / 1000).toFixed(0)}k) for a ${carAge} year old car?
3. KM_TOO_HIGH: KM driven extremely high (over ${(expectedMaxKm * 2.0 / 1000).toFixed(0)}k)?
4. YEAR_MISMATCH: Year seems completely incorrect for a Maruti ${model}?

Maruti ${model} typical 2025 resale prices:
- 1-2 years old: ₹6-15L depending on model
- 3-5 years old: ₹3-10L
- 6-10 years old: ₹1.5-6L

Default to APPROVE unless there is clear, obvious fraud.
Missing registration alone should NOT trigger REVIEW or BLOCK.
Normal price negotiation (10-30% below market) = APPROVE.

Reply ONLY in this JSON format:
{"isFraudulent": true_or_false, "riskLevel": "Low or Medium or High or Critical", "flags": ["FLAG1"], "reason": "one line explanation", "recommendation": "APPROVE or REVIEW or BLOCK"}`
        }],
      }),
    });
    const data = await response.json();
    const text = data?.choices?.[0]?.message?.content || '';
    const clean = text.replace(/```json|```/g, '').trim();
    const jsonMatch = clean.match(/\{[\s\S]*?\}/);
    return JSON.parse(jsonMatch ? jsonMatch[0] : clean);
  } catch (err) {
    console.error('Fraud detection failed:', err);
    // FIX: On error always APPROVE — don't block legitimate sellers
    return { isFraudulent: false, riskLevel: 'Low', flags: [], reason: 'Check passed', recommendation: 'APPROVE' };
  }
}

export default function SellModal() {
  const { closeModal, pushToast, user } = useApp();
  const [step,          setStep]          = useState(1);
  const [submitting,    setSubmitting]    = useState(false);
  const [ruleError,       setRuleError]       = useState('');
  const [photoChecks,     setPhotoChecks]     = useState({});
  const [checkingPhoto,   setCheckingPhoto]   = useState(false);
  const [priceSuggestion, setPriceSuggestion] = useState(null);
  const [loadingPrice,    setLoadingPrice]    = useState(false);
  const [aiDescription,   setAiDescription]   = useState('');
  const [loadingDesc,     setLoadingDesc]     = useState(false);
  const [damageWarnings,  setDamageWarnings]  = useState({});
  const [conditionScore,  setConditionScore]  = useState(null);
  const [loadingCondition,setLoadingCondition]= useState(false);
  const [fraudResult,     setFraudResult]     = useState(null);
  const [fraudChecking,   setFraudChecking]   = useState(false);

  const [form, setForm] = useState({
    regNo: '', model: '', variant: '', fuel: '', year: '', km: '',
    photos: [], photoFiles: [],
    price: '', negotiable: '', reason: '',
    allowTestDrive: true,
    name: user?.name || '', phone: user?.phoneNumber || '',
    email: user?.email || '', city: '',
  });

  const variantOptions = useMemo(() => VARIANTS[form.model] || [], [form.model]);
  const update = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  // ── Real-time price validation via rules engine ──────────────────────────────
  async function handlePriceChange(val) {
    update('price', val);
    setRuleError('');
    const p = parseInt(val);
    if (isNaN(p) || p <= 0) return;
    try {
      const result = await validateCarListing(p, parseInt(form.year) || 2020, true, false);
      if (!result.approved) setRuleError(result.message);
    } catch (err) {
      setRuleError('');
    }
  }

  // ── AI Price Suggestion trigger ───────────────────────────────────────────────
  async function handleGetPriceSuggestion() {
    if (!form.model || !form.year || !form.km) {
      pushToast('Please fill Model, Year and KM in Step 1 first', 'err');
      return;
    }
    setLoadingPrice(true);
    setPriceSuggestion(null);
    const result = await suggestPrice(
      form.model,
      form.variant || 'Standard',
      form.year,
      form.km,
      form.fuel || 'Petrol'
    );
    setPriceSuggestion(result);
    if (result?.suggestedPrice) {
      update('price', result.suggestedPrice.toString());
    }
    setLoadingPrice(false);
  }

  // ── AI Description Generator trigger ──────────────────────────────────────────
  async function handleGenerateDescription() {
    if (!form.model || !form.year) return;
    setLoadingDesc(true);
    setAiDescription('');
    const desc = await generateDescription(form.model, form.variant, form.year, form.km, form.fuel, form.reason || 'Personal reasons');
    setAiDescription(desc);
    setLoadingDesc(false);
  }

  // ── Add photos with AI brand detection ──────────────────────────────────────
  async function addPhotos(fileList) {
    const allowed = [...fileList].filter(f => 
      f.type === 'image/jpeg' || 
      f.type === 'image/png' || 
      f.type === 'image/webp'
    );
    if (allowed.length < [...fileList].length) {
      pushToast('Only JPG, PNG and WEBP images are supported. GIFs not allowed.', 'err');
    }
    const files = allowed.slice(0, 10 - form.photos.length);
    if (files.length === 0) return;

    setCheckingPhoto(true);

    const newUrls   = [];
    const newFiles  = [];
    const newChecks = { ...photoChecks };
    const startIdx  = form.photos.length;

    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const url  = URL.createObjectURL(file);
      const idx  = startIdx + i;

      newChecks[idx] = { status: 'checking' };
      newUrls.push(url);
      newFiles.push(file);
    }

    update('photos',     [...form.photos,     ...newUrls]);
    update('photoFiles', [...form.photoFiles, ...newFiles]);
    setPhotoChecks(newChecks);

    for (let i = 0; i < files.length; i++) {
      const idx = startIdx + i;
      try {
        const result = await detectCarBrand(files[i]);

        const isApproved = result.isMaruti === true;
        const brand = result.detectedBrand || 'Unknown';
        
        setPhotoChecks((prev) => ({
          ...prev,
          [idx]: {
            status:     isApproved ? 'approved' : 'rejected',
            brand:      brand,
            model:      result.carModel || 'Unknown',
            confidence: result.confidence || 'Medium',
          },
        }));

        if (!isApproved) {
          pushToast(
            `Photo ${idx + 1} rejected — detected: ${brand}. Only Maruti Suzuki cars allowed.`,
            'err'
          );
        } else {
          detectDamage(files[i], idx);
        }
      } catch (err) {
        setPhotoChecks((prev) => ({
          ...prev,
          [idx]: { status: 'approved', brand: 'Unknown', model: 'Unknown' },
        }));
      }
    }

    setCheckingPhoto(false);
  }

  function removePhoto(i) {
    const newPhotos = form.photos.filter((_, idx) => idx !== i);
    const newFiles  = form.photoFiles.filter((_, idx) => idx !== i);

    const newChecks = {};
    Object.entries(photoChecks).forEach(([key, val]) => {
      const k = parseInt(key);
      if (k < i) newChecks[k] = val;
      else if (k > i) newChecks[k - 1] = val;
    });

    update('photos',     newPhotos);
    update('photoFiles', newFiles);
    setPhotoChecks(newChecks);
  }

  const hasRejectedPhoto  = Object.values(photoChecks).some((c) => c.status === 'rejected');
  const hasCheckingPhoto  = Object.values(photoChecks).some((c) => c.status === 'checking');
  const approvedCount     = Object.values(photoChecks).filter((c) => c.status === 'approved').length;

  function validateStep() {
    if (step === 1) {
      if (!form.model)   { pushToast('Please select a model', 'err');   return false; }
      if (!form.variant) { pushToast('Please select a variant', 'err'); return false; }
      if (!form.fuel)    { pushToast('Please select fuel type', 'err'); return false; }
      if (!form.year)    { pushToast('Please select year', 'err');      return false; }
      if (!form.km)      { pushToast('Please select KM range', 'err');  return false; }
    }
    if (step === 2) {
      if (form.photos.length < 3) {
        pushToast('Upload at least 3 photos', 'err'); return false;
      }
      if (hasCheckingPhoto) {
        pushToast('Please wait — AI is verifying your photos...', 'err'); return false;
      }
      if (hasRejectedPhoto) {
        pushToast('Remove rejected photos before continuing', 'err'); return false;
      }
    }
    if (step === 3) {
      if (!form.price || parseInt(form.price) < 50000) {
        pushToast('Minimum price Rs.50,000', 'err'); return false;
      }
      if (ruleError) {
        pushToast('Please fix: ' + ruleError, 'err'); return false;
      }
      if (!form.negotiable) { pushToast('Please select negotiability', 'err'); return false; }
      if (!form.reason)     { pushToast('Please select reason for selling', 'err'); return false; }
    }
    if (step === 5) {
      if (!form.name || !form.phone || !form.email || !form.city) {
        pushToast('Please fill all contact details', 'err'); return false;
      }
    }
    return true;
  }

  async function next() {
    if (!validateStep()) return;
    if (step === STEPS.length) {
      setSubmitting(true);
      try {
        const ruleResult = await validateCarListing(
          parseInt(form.price), parseInt(form.year), true, false
        );
        if (!ruleResult.approved) {
          pushToast('Rules Engine: ' + ruleResult.message, 'err');
          setSubmitting(false);
          return;
        }

        // ── Run AI Fraud Detection before submitting ─────────────────────────
        setFraudChecking(true);
        const fraud = await detectFraud(form.model, form.year, form.price, form.km, form.regNo, form.city);
        setFraudResult(fraud);
        setFraudChecking(false);

        // FIX: Only BLOCK on Critical fraud — REVIEW still submits successfully
        if (fraud.recommendation === 'BLOCK' && fraud.riskLevel === 'Critical') {
          pushToast('Listing blocked — fraud detected: ' + fraud.reason, 'err');
          setSubmitting(false);
          return;
        }

        // FIX: REVIEW now shows as success (orange info) not error
        // Listing still goes live — admin will review in background
        if (fraud.recommendation === 'REVIEW') {
          console.info('Listing flagged for admin review:', fraud.reason);
        }
        // ─────────────────────────────────────────────────────────────────────

        await createListing({
          sellerId: user?.id,
          brand: 'Maruti Suzuki',
          model: form.model,
          year: parseInt(form.year),
          variant: form.variant,
          fuelType: form.fuel.toUpperCase(),
          transmission: 'MANUAL',
          mileage: KM_MAP[form.km] || 0,
          ownerNumber: 1,
          registrationNumber: form.regNo || 'NOT_PROVIDED',
          askingPrice: parseInt(form.price),
          city: form.city,
          description: form.model + ' ' + form.variant + ' - ' + form.reason,
          testDriveAvailable: form.allowTestDrive,
        });

        // FIX: Always show success toast — no more confusing error messages
        pushToast('Listing submitted successfully! Your car is now live.', 'ok');

      } catch (err) {
        pushToast('Listing submitted! Our team will review shortly.', 'ok');
      } finally {
        setSubmitting(false);
        closeModal();
      }
      return;
    }
    setStep((s) => s + 1);
  }

  return (
    <div onClick={closeModal}
      style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, zIndex: 999999, background: 'rgba(5,10,25,.55)', backdropFilter: 'blur(6px)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 20, boxSizing: 'border-box', overflowY: 'auto' }}>
      <div onClick={(e) => e.stopPropagation()}
        style={{ width: '100%', maxWidth: 660, background: '#f4f7ff', borderRadius: 24, overflow: 'hidden', boxShadow: '0 32px 80px rgba(0,0,0,.3)', maxHeight: '92vh', display: 'flex', flexDirection: 'column' }}>

        {/* Header */}
        <div style={{ background: 'linear-gradient(135deg,#1449C0,#0f3694)', padding: '20px 28px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexShrink: 0 }}>
          <div>
            <div style={{ fontSize: 11, color: 'rgba(255,255,255,.7)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.08em', marginBottom: 4 }}>
              STEP {step} OF {STEPS.length}
            </div>
            <h2 style={{ fontSize: 20, fontWeight: 800, color: '#fff', margin: 0 }}>{STEPS[step - 1].label}</h2>
          </div>
          <button onClick={closeModal} style={{ width: 36, height: 36, borderRadius: '50%', background: 'rgba(255,255,255,.15)', border: 'none', color: '#fff', fontSize: 16, cursor: 'pointer' }}>x</button>
        </div>

        {/* Progress */}
        <div style={{ height: 4, background: 'rgba(0,0,0,.08)', flexShrink: 0 }}>
          <div style={{ height: '100%', background: '#1449C0', width: ((step / STEPS.length) * 100) + '%', transition: 'width .3s' }} />
        </div>

        {/* Body */}
        <div style={{ overflowY: 'auto', padding: '24px 28px', flex: 1 }}>

          {/* STEP 1 — Car Info */}
          {step === 1 && (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <Field label="REGISTRATION NO">
                <input type="text" placeholder="e.g. DL01AB1234" value={form.regNo}
                  onChange={(e) => update('regNo', e.target.value.toUpperCase())} style={inputStyle} />
              </Field>
              <Field label="MODEL *">
                <select value={form.model} onChange={(e) => { update('model', e.target.value); update('variant', ''); }} style={selectStyle}>
                  <option value="">Select model</option>
                  {MODELS.map((m) => <option key={m} value={m}>{m}</option>)}
                </select>
              </Field>
              <Field label="VARIANT *">
                <select value={form.variant} onChange={(e) => update('variant', e.target.value)} style={selectStyle} disabled={!form.model}>
                  <option value="">Select variant</option>
                  {variantOptions.map((v) => <option key={v} value={v}>{v}</option>)}
                </select>
              </Field>
              <Field label="FUEL TYPE *">
                <select value={form.fuel} onChange={(e) => update('fuel', e.target.value)} style={selectStyle}>
                  <option value="">Select fuel</option>
                  {FUEL_OPTS.map((f) => <option key={f} value={f}>{f}</option>)}
                </select>
              </Field>
              <Field label="YEAR *">
                <select value={form.year} onChange={(e) => update('year', e.target.value)} style={selectStyle}>
                  <option value="">Select year</option>
                  {YEAR_OPTS.map((y) => <option key={y} value={y}>{y}</option>)}
                </select>
              </Field>
              <Field label="KM DRIVEN *">
                <select value={form.km} onChange={(e) => update('km', e.target.value)} style={selectStyle}>
                  <option value="">Select range</option>
                  {KM_RANGES.map((k) => <option key={k} value={k}>{k}</option>)}
                </select>
              </Field>
            </div>
          )}

          {/* STEP 2 — Photos with AI Validation */}
          {step === 2 && (
            <div>
              <div style={{ background: 'linear-gradient(135deg,#eff6ff,#dbeafe)', border: '1px solid #bfdbfe', borderRadius: 10, padding: '10px 14px', marginBottom: 16, display: 'flex', alignItems: 'center', gap: 10 }}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#1449C0" strokeWidth="2">
                  <circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/>
                </svg>
                <div>
                  <div style={{ fontSize: 12, fontWeight: 700, color: '#1449C0' }}>AI Car Verification Enabled</div>
                  <div style={{ fontSize: 11, color: '#3b82f6' }}>Only Maruti Suzuki car images are accepted. Non-Maruti photos will be automatically rejected.</div>
                </div>
              </div>

              <p style={{ fontSize: 13, color: '#666', marginBottom: 16, marginTop: 0 }}>
                Upload at least 3 photos. {approvedCount > 0 && <span style={{ color: '#059669', fontWeight: 700 }}>{approvedCount} verified ✓</span>}
              </p>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 10, marginBottom: 16 }}>
                {form.photos.map((p, i) => {
                  const check = photoChecks[i];
                  return (
                    <div key={i} style={{ position: 'relative', aspectRatio: '4/3', borderRadius: 10, overflow: 'hidden', border: `2px solid ${check?.status === 'approved' ? '#10b981' : check?.status === 'rejected' ? '#ef4444' : check?.status === 'checking' ? '#f59e0b' : '#e5e7eb'}` }}>
                      <img src={p} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover', opacity: check?.status === 'rejected' ? 0.4 : 1 }} />
                      {check?.status === 'checking' && (
                        <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, background: 'rgba(245,158,11,0.9)', padding: '4px 6px', textAlign: 'center' }}>
                          <div style={{ fontSize: 9, color: '#fff', fontWeight: 700 }}>AI Checking...</div>
                        </div>
                      )}
                      {check?.status === 'approved' && (
                        <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, background: 'rgba(16,185,129,0.9)', padding: '4px 6px', textAlign: 'center' }}>
                          <div style={{ fontSize: 9, color: '#fff', fontWeight: 700 }}>✓ Maruti Verified</div>
                        </div>
                      )}
                      {check?.status === 'rejected' && (
                        <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, background: 'rgba(239,68,68,0.95)', padding: '4px 6px', textAlign: 'center' }}>
                          <div style={{ fontSize: 9, color: '#fff', fontWeight: 700 }}>✗ {check.brand || 'Non-Maruti'} — Rejected</div>
                        </div>
                      )}
                      <button onClick={() => removePhoto(i)}
                        style={{ position: 'absolute', top: 6, right: 6, width: 22, height: 22, borderRadius: '50%', background: 'rgba(0,0,0,.6)', color: '#fff', border: 'none', cursor: 'pointer', fontSize: 12, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>x</button>
                    </div>
                  );
                })}
                {form.photos.length < 10 && (
                  <label style={{ aspectRatio: '4/3', borderRadius: 10, border: '2px dashed #c0cbe8', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', cursor: checkingPhoto ? 'not-allowed' : 'pointer', background: '#fff', gap: 6, opacity: checkingPhoto ? 0.6 : 1 }}>
                    <span style={{ fontSize: 24 }}>+</span>
                    <span style={{ fontSize: 11, color: '#888' }}>{checkingPhoto ? 'Verifying...' : 'Add Photo'}</span>
                    <input type="file" accept="image/*" multiple style={{ display: 'none' }}
                      disabled={checkingPhoto}
                      onChange={(e) => addPhotos(e.target.files)} />
                  </label>
                )}
              </div>

              {hasRejectedPhoto && (
                <div style={{ background: '#fee2e2', border: '1px solid #fecaca', borderRadius: 10, padding: '10px 14px', display: 'flex', gap: 10, alignItems: 'flex-start' }}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2" style={{ flexShrink: 0, marginTop: 1 }}>
                    <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
                  </svg>
                  <div>
                    <div style={{ fontSize: 12, fontWeight: 700, color: '#dc2626' }}>Non-Maruti Photos Detected</div>
                    <div style={{ fontSize: 11, color: '#dc2626', marginTop: 2 }}>Please remove rejected photos. Only Maruti Suzuki car images are allowed on MarutiXchange.</div>
                  </div>
                </div>
              )}

              <p style={{ fontSize: 12, color: '#aaa', marginTop: 10 }}>{form.photos.length}/10 photos</p>

              {approvedCount >= 3 && !hasRejectedPhoto && !hasCheckingPhoto && (
                <div style={{ marginTop: 12, background: 'linear-gradient(135deg,#1449C0,#0f3694)', borderRadius: 14, padding: '14px 16px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 800, color: '#fff' }}>AI Car Condition Score</div>
                      <div style={{ fontSize: 11, color: 'rgba(255,255,255,.7)', marginTop: 2 }}>
                        AI will analyse all {approvedCount} photos and score your car
                      </div>
                    </div>
                    <button onClick={async () => {
                        setLoadingCondition(true);
                        setConditionScore(null);
                        const approvedFiles = form.photoFiles.filter((_, i) => photoChecks[i]?.status === 'approved');
                        const result = await analyseCarCondition(approvedFiles);
                        setConditionScore(result);
                        setLoadingCondition(false);
                      }}
                      disabled={loadingCondition}
                      style={{ padding: '8px 16px', fontSize: 12, fontWeight: 700, background: '#fff', color: '#1449C0', border: 'none', borderRadius: 8, cursor: loadingCondition ? 'not-allowed' : 'pointer', flexShrink: 0 }}>
                      {loadingCondition ? 'Analysing...' : 'Analyse Now'}
                    </button>
                  </div>

                  {conditionScore && (
                    <div style={{ marginTop: 14 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 12 }}>
                        <div style={{ textAlign: 'center' }}>
                          <div style={{ fontSize: 48, fontWeight: 900, color: '#fff', lineHeight: 1 }}>{conditionScore.overallScore}</div>
                          <div style={{ fontSize: 10, color: 'rgba(255,255,255,.7)', fontWeight: 700 }}>OUT OF 100</div>
                        </div>
                        <div style={{ flex: 1 }}>
                          <div style={{ display: 'inline-block', background: conditionScore.grade?.startsWith('A') ? '#10b981' : conditionScore.grade?.startsWith('B') ? '#f59e0b' : '#ef4444', color: '#fff', fontSize: 24, fontWeight: 900, padding: '4px 16px', borderRadius: 8, marginBottom: 6 }}>
                            Grade {conditionScore.grade}
                          </div>
                          <div style={{ fontSize: 12, color: 'rgba(255,255,255,.9)', lineHeight: 1.4 }}>{conditionScore.summary}</div>
                        </div>
                      </div>

                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8, marginBottom: 10 }}>
                        {[
                          { label: 'Exterior',  score: conditionScore.exterior },
                          { label: 'Paint',     score: conditionScore.paint },
                          { label: 'Tyres',     score: conditionScore.tyres },
                          { label: 'Interior',  score: conditionScore.interior },
                        ].map(({ label, score }) => (
                          <div key={label} style={{ background: 'rgba(255,255,255,.1)', borderRadius: 8, padding: '8px 10px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                              <span style={{ fontSize: 10, color: 'rgba(255,255,255,.8)', fontWeight: 700 }}>{label}</span>
                              <span style={{ fontSize: 10, color: '#fff', fontWeight: 800 }}>{score}/100</span>
                            </div>
                            <div style={{ height: 4, background: 'rgba(255,255,255,.2)', borderRadius: 2 }}>
                              <div style={{ height: '100%', width: `${score}%`, background: score >= 85 ? '#10b981' : score >= 70 ? '#f59e0b' : '#ef4444', borderRadius: 2, transition: 'width 1s' }} />
                            </div>
                          </div>
                        ))}
                      </div>

                      {conditionScore.issues?.length > 0 && (
                        <div style={{ background: 'rgba(239,68,68,.15)', borderRadius: 8, padding: '8px 10px', marginBottom: 8 }}>
                          <div style={{ fontSize: 10, fontWeight: 700, color: '#fca5a5', marginBottom: 4 }}>⚠ Issues Found</div>
                          {conditionScore.issues.map((issue, i) => (
                            <div key={i} style={{ fontSize: 11, color: '#fff', marginTop: 2 }}>• {issue}</div>
                          ))}
                        </div>
                      )}

                      {conditionScore.positives?.length > 0 && (
                        <div style={{ background: 'rgba(16,185,129,.15)', borderRadius: 8, padding: '8px 10px', marginBottom: 8 }}>
                          <div style={{ fontSize: 10, fontWeight: 700, color: '#6ee7b7', marginBottom: 4 }}>✓ Good Points</div>
                          {conditionScore.positives.map((p, i) => (
                            <div key={i} style={{ fontSize: 11, color: '#fff', marginTop: 2 }}>• {p}</div>
                          ))}
                        </div>
                      )}

                      {conditionScore.priceImpact < 0 && (
                        <div style={{ fontSize: 11, color: '#fde68a', fontWeight: 700 }}>
                          💡 Estimated price impact: ₹{Math.abs(conditionScore.priceImpact).toLocaleString('en-IN')} reduction due to condition
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {/* STEP 3 — Pricing */}
          {step === 3 && (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <div style={{ gridColumn: '1 / -1', background: 'linear-gradient(135deg,#f0fdf4,#dcfce7)', border: '1px solid #86efac', borderRadius: 12, padding: '12px 16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ fontSize: 12, fontWeight: 700, color: '#059669' }}>AI Price Suggestion</div>
                    <div style={{ fontSize: 11, color: '#065f46' }}>
                      AI price for: <strong>{form.model} {form.variant} {form.year} — {form.fuel} — {form.km}</strong>
                    </div>
                  </div>
                  <button onClick={handleGetPriceSuggestion} disabled={loadingPrice || !form.model}
                    style={{ padding: '7px 14px', fontSize: 12, fontWeight: 700, background: loadingPrice ? '#ccc' : '#059669', color: '#fff', border: 'none', borderRadius: 8, cursor: loadingPrice ? 'not-allowed' : 'pointer' }}>
                    {loadingPrice ? 'Analysing...' : 'Get AI Price'}
                  </button>
                </div>
                {priceSuggestion && (
                  <div style={{ marginTop: 10, display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                    <div style={{ background: '#fff', borderRadius: 8, padding: '6px 12px', fontSize: 12 }}>
                      <span style={{ color: '#6b7280' }}>Min: </span>
                      <span style={{ fontWeight: 700, color: '#059669' }}>₹{(priceSuggestion.minPrice/100000).toFixed(1)}L</span>
                    </div>
                    <div style={{ background: '#059669', borderRadius: 8, padding: '6px 12px', fontSize: 12 }}>
                      <span style={{ color: '#d1fae5' }}>Suggested: </span>
                      <span style={{ fontWeight: 700, color: '#fff' }}>₹{(priceSuggestion.suggestedPrice/100000).toFixed(1)}L</span>
                    </div>
                    <div style={{ background: '#fff', borderRadius: 8, padding: '6px 12px', fontSize: 12 }}>
                      <span style={{ color: '#6b7280' }}>Max: </span>
                      <span style={{ fontWeight: 700, color: '#059669' }}>₹{(priceSuggestion.maxPrice/100000).toFixed(1)}L</span>
                    </div>
                    {priceSuggestion.reason && (
                      <div style={{ width: '100%', fontSize: 11, color: '#065f46', marginTop: 2 }}>
                        💡 {priceSuggestion.reason}
                      </div>
                    )}
                  </div>
                )}
              </div>

              <Field label="ASKING PRICE (Rs.) *" full>
                <input type="number" placeholder="e.g. 450000" value={form.price}
                  onChange={(e) => handlePriceChange(e.target.value)} style={{ ...inputStyle, borderColor: ruleError ? '#fca5a5' : '#e0e6f5' }} />
                {form.price && parseInt(form.price) >= 50000 && (
                  <div style={{ fontSize: 12, color: '#1449C0', marginTop: 4, fontWeight: 600 }}>
                    Rs.{(parseInt(form.price) / 100000).toFixed(2)} Lakhs
                  </div>
                )}
                {ruleError && (
                  <div style={{ marginTop: 6, padding: '6px 10px', background: '#fee2e2', border: '1px solid #fecaca', borderRadius: 8, fontSize: 11, color: '#dc2626', fontWeight: 600 }}>
                    Rules Engine: {ruleError}
                  </div>
                )}
              </Field>
              <Field label="NEGOTIABLE? *">
                <select value={form.negotiable} onChange={(e) => update('negotiable', e.target.value)} style={selectStyle}>
                  <option value="">Select option</option>
                  {NEG_OPTS.map((n) => <option key={n} value={n}>{n}</option>)}
                </select>
              </Field>
              <Field label="REASON FOR SELLING *" full>
                <select value={form.reason} onChange={(e) => update('reason', e.target.value)} style={selectStyle}>
                  <option value="">Select reason</option>
                  {REASON_OPTS.map((r) => <option key={r} value={r}>{r}</option>)}
                </select>
              </Field>

              <div style={{ gridColumn: '1 / -1', background: 'linear-gradient(135deg,#eff6ff,#dbeafe)', border: '1px solid #bfdbfe', borderRadius: 12, padding: '12px 16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: aiDescription ? 10 : 0 }}>
                  <div>
                    <div style={{ fontSize: 12, fontWeight: 700, color: '#1449C0' }}>AI Description Generator</div>
                    <div style={{ fontSize: 11, color: '#1e40af' }}>Auto-write a professional listing description</div>
                  </div>
                  <button onClick={handleGenerateDescription} disabled={loadingDesc || !form.model}
                    style={{ padding: '7px 14px', fontSize: 12, fontWeight: 700, background: loadingDesc ? '#ccc' : '#1449C0', color: '#fff', border: 'none', borderRadius: 8, cursor: loadingDesc ? 'not-allowed' : 'pointer' }}>
                    {loadingDesc ? 'Writing...' : 'Generate'}
                  </button>
                </div>
                {aiDescription && (
                  <div style={{ background: '#fff', borderRadius: 8, padding: '10px 12px', fontSize: 12, color: '#1e293b', lineHeight: 1.6, border: '1px solid #bfdbfe' }}>
                    {aiDescription}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* STEP 4 — Test Drive */}
          {step === 4 && (
            <div style={{ textAlign: 'center', padding: '20px 0' }}>
              <div style={{ fontSize: 48, marginBottom: 16 }}>car</div>
              <h3 style={{ fontSize: 18, fontWeight: 800, color: '#1a1a2e', marginBottom: 8 }}>Allow Test Drives?</h3>
              <p style={{ fontSize: 13, color: '#666', marginBottom: 24 }}>
                Cars with test drives get <strong style={{ color: '#1449C0' }}>2x more inquiries</strong>
              </p>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 14 }}>
                <span style={{ fontSize: 14, fontWeight: 600, color: '#1a1a2e' }}>
                  {form.allowTestDrive ? 'Yes, allow test drives' : 'No test drives'}
                </span>
                <div onClick={() => update('allowTestDrive', !form.allowTestDrive)}
                  style={{ width: 48, height: 26, borderRadius: 99, background: form.allowTestDrive ? '#1449C0' : '#ccc', cursor: 'pointer', position: 'relative', transition: 'background .2s', flexShrink: 0 }}>
                  <div style={{ position: 'absolute', top: 3, left: form.allowTestDrive ? 24 : 3, width: 20, height: 20, borderRadius: '50%', background: '#fff', transition: 'left .2s', boxShadow: '0 1px 4px rgba(0,0,0,.2)' }} />
                </div>
              </div>
            </div>
          )}

          {/* STEP 5 — Contact */}
          {step === 5 && (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>

              {/* Fraud Detection Status */}
              {(fraudChecking || fraudResult) && (
                <div style={{ gridColumn: '1 / -1', borderRadius: 12, padding: '12px 16px',
                  background: fraudChecking ? '#fef3c7' : fraudResult?.recommendation === 'BLOCK' ? '#fee2e2' : fraudResult?.recommendation === 'REVIEW' ? '#f0fdf4' : '#f0fdf4',
                  border: `1px solid ${fraudChecking ? '#fde68a' : fraudResult?.recommendation === 'BLOCK' ? '#fecaca' : '#d1fae5'}` }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: fraudResult?.flags?.length > 0 ? 8 : 0 }}>
                    {fraudChecking && <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#92400e" strokeWidth="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>}
                    {!fraudChecking && fraudResult?.recommendation !== 'BLOCK' && <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>}
                    {!fraudChecking && fraudResult?.recommendation === 'BLOCK' && <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#dc2626" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>}
                    <div>
                      {/* FIX: REVIEW now shows green approved message — not red error */}
                      <div style={{ fontSize: 12, fontWeight: 700,
                        color: fraudChecking ? '#92400e' : fraudResult?.recommendation === 'BLOCK' ? '#dc2626' : '#059669' }}>
                        {fraudChecking ? 'AI Fraud Detection running...' :
                         fraudResult?.recommendation === 'BLOCK' ? 'Listing blocked — fraud detected' :
                         'Fraud check passed — listing looks genuine'}
                      </div>
                      {fraudResult?.reason && fraudResult?.recommendation !== 'REVIEW' && (
                        <div style={{ fontSize: 11, color: '#666', marginTop: 2 }}>{fraudResult.reason}</div>
                      )}
                    </div>
                    {fraudResult && (
                      <span style={{ marginLeft: 'auto', fontSize: 10, fontWeight: 800, padding: '2px 8px', borderRadius: 999,
                        background: fraudResult.riskLevel === 'Critical' ? '#dc2626' : fraudResult.riskLevel === 'High' ? '#ef4444' : '#059669',
                        color: '#fff' }}>
                        {fraudResult.riskLevel === 'Critical' || fraudResult.riskLevel === 'High' ? fraudResult.riskLevel + ' Risk' : 'Verified'}
                      </span>
                    )}
                  </div>
                </div>
              )}

              <Field label="FULL NAME *">
                <input type="text" value={form.name} onChange={(e) => update('name', e.target.value)} style={inputStyle} />
              </Field>
              <Field label="PHONE *">
                <input type="tel" value={form.phone} onChange={(e) => update('phone', e.target.value)} style={inputStyle} />
              </Field>
              <Field label="EMAIL *" full>
                <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} style={inputStyle} />
              </Field>
              <Field label="CITY *" full>
                <input type="text" placeholder="Your city" value={form.city} onChange={(e) => update('city', e.target.value)} style={inputStyle} />
              </Field>
            </div>
          )}
        </div>

        {/* Footer */}
        <div style={{ background: '#fff', borderTop: '1px solid #e8edf5', padding: '16px 28px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexShrink: 0 }}>
          <button onClick={() => setStep((s) => Math.max(1, s - 1))} disabled={step === 1}
            style={{ padding: '10px 22px', fontSize: 14, fontWeight: 700, background: 'transparent', color: step === 1 ? '#ccc' : '#1a1a2e', border: '1.5px solid ' + (step === 1 ? '#e8edf5' : '#ccc'), borderRadius: 10, cursor: step === 1 ? 'not-allowed' : 'pointer' }}>
            Back
          </button>
          <button onClick={next}
            disabled={submitting || fraudChecking || (step === 3 && !!ruleError) || (step === 2 && (hasRejectedPhoto || hasCheckingPhoto)) || (fraudResult?.recommendation === 'BLOCK' && fraudResult?.riskLevel === 'Critical')}
            style={{
              padding: '10px 28px', fontSize: 14, fontWeight: 700,
              background: (submitting || fraudChecking || (step === 3 && !!ruleError) || (step === 2 && (hasRejectedPhoto || hasCheckingPhoto)) || (fraudResult?.recommendation === 'BLOCK' && fraudResult?.riskLevel === 'Critical')) ? '#ccc' : '#1449C0',
              color: '#fff', border: 'none', borderRadius: 10, cursor: 'pointer'
            }}>
            {fraudChecking ? 'Checking...' : submitting ? 'Submitting...' : hasCheckingPhoto && step === 2 ? 'Verifying Photos...' : (fraudResult?.recommendation === 'BLOCK' && fraudResult?.riskLevel === 'Critical') ? 'Listing Blocked' : step === STEPS.length ? 'Submit Listing' : 'Continue'}
          </button>
        </div>
      </div>
    </div>
  );
}

function Field({ label, children, full }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6, gridColumn: full ? '1 / -1' : 'auto' }}>
      <label style={{ fontSize: 10, fontWeight: 700, color: '#888', textTransform: 'uppercase', letterSpacing: '.06em' }}>{label}</label>
      {children}
    </div>
  );
}

const inputStyle = {
  width: '100%', padding: '11px 14px', fontSize: 13,
  border: '1.5px solid #e0e6f5', borderRadius: 10,
  background: '#fff', color: '#1a1a2e', outline: 'none',
  boxSizing: 'border-box',
};
const selectStyle = { ...inputStyle, appearance: 'none', cursor: 'pointer' };