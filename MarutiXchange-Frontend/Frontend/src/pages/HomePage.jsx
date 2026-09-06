import Hero from '../components/home/Hero';
import FeaturedCars from '../components/home/FeaturedCars';
import TrustStrip from '../components/home/TrustStrip';
import SellBanner from '../components/home/SellBanner';
import Testimonials from '../components/home/Testimonials';
import HowItWorks from '../components/home/HowItWorks';
import WhyMaruti from '../components/home/WhyMaruti';

export default function HomePage() {
  return (
    <div>
      <Hero />
      <TrustStrip />
      <FeaturedCars />
      <HowItWorks />
      <WhyMaruti />
      <SellBanner />
      <Testimonials />
    </div>
  );
}