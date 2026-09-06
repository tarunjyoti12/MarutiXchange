const shimmerStyle = `@keyframes mx-shimmer{0%{background-position:-600px 0}100%{background-position:600px 0}}.mx-skeleton{background:linear-gradient(90deg,var(--bg2) 25%,var(--bd1) 37%,var(--bg2) 63%);background-size:600px 100%;animation:mx-shimmer 1.4s ease infinite;border-radius:6px;}`;
if(typeof document!=='undefined'&&!document.getElementById('mx-skeleton-style')){const el=document.createElement('style');el.id='mx-skeleton-style';el.textContent=shimmerStyle;document.head.appendChild(el);}

export function Skeleton({width='100%',height=16,radius=6,style={}}){
  return <div className="mx-skeleton" style={{width,height,borderRadius:radius,flexShrink:0,...style}}/>;
}

export function SkeletonCard(){
  return(
    <div style={{background:'var(--cbg)',border:'1px solid var(--bd1)',borderRadius:20,overflow:'hidden'}}>
      <Skeleton width="100%" height={180} radius={0}/>
      <div style={{padding:'14px 16px'}}>
        <div style={{display:'flex',justifyContent:'space-between',marginBottom:10}}>
          <Skeleton width="55%" height={16}/>
          <Skeleton width="22%" height={16}/>
        </div>
        <Skeleton width="75%" height={13} style={{marginBottom:12}}/>
        <div style={{display:'flex',justifyContent:'space-between'}}>
          <Skeleton width="35%" height={12}/>
          <Skeleton width="18%" height={12}/>
        </div>
      </div>
    </div>
  );
}

export function SkeletonStatCard(){
  return(
    <div style={{background:'var(--cbg)',border:'1px solid var(--bd1)',borderRadius:16,padding:18}}>
      <Skeleton width="50%" height={11} style={{marginBottom:10}}/>
      <Skeleton width="40%" height={28} style={{marginBottom:8}}/>
      <Skeleton width="60%" height={12}/>
    </div>
  );
}

export function SkeletonListingRow(){
  return(
    <div style={{display:'flex',alignItems:'center',gap:16,padding:'14px 16px',borderBottom:'1px solid var(--bd0)'}}>
      <Skeleton width={48} height={34} radius={8} style={{flexShrink:0}}/>
      <div style={{flex:2}}>
        <Skeleton width="70%" height={14} style={{marginBottom:6}}/>
        <Skeleton width="40%" height={11}/>
      </div>
      <Skeleton width="15%" height={14} style={{flex:1}}/>
      <Skeleton width="10%" height={14} style={{flex:1}}/>
      <Skeleton width="10%" height={14} style={{flex:1}}/>
      <Skeleton width={60} height={26} radius={8} style={{flexShrink:0}}/>
      <Skeleton width={50} height={26} radius={8} style={{flexShrink:0}}/>
    </div>
  );
}

export function SkeletonBidCard(){
  return(
    <div style={{background:'var(--cbg)',border:'1px solid var(--bd1)',borderRadius:20,overflow:'hidden'}}>
      <Skeleton width="100%" height={190} radius={0}/>
      <div style={{padding:18}}>
        <div style={{display:'flex',justifyContent:'space-between',marginBottom:10}}>
          <Skeleton width="55%" height={16}/>
          <Skeleton width="20%" height={16}/>
        </div>
        <Skeleton width="80%" height={13} style={{marginBottom:12}}/>
        <div style={{display:'flex',gap:10,marginBottom:12}}>
          <Skeleton width="40%" height={36} radius={10}/>
          <Skeleton width="55%" height={36} radius={10}/>
        </div>
        <Skeleton width="100%" height={40} radius={12}/>
      </div>
    </div>
  );
}

export function SkeletonCarDetail(){
  return(
    <div style={{display:'grid',gridTemplateColumns:'1fr 360px',minHeight:500}}>
      <div style={{borderRight:'1px solid var(--bd1)'}}>
        <Skeleton width="100%" height={340} radius={0}/>
        <div style={{padding:24}}>
          <Skeleton width="40%" height={18} style={{marginBottom:20}}/>
          <div style={{display:'grid',gridTemplateColumns:'repeat(3,1fr)',gap:10}}>
            {[1,2,3,4,5,6,7,8,9].map((i)=>(
              <div key={i} style={{background:'var(--bg2)',borderRadius:10,padding:'12px 14px'}}>
                <Skeleton width="60%" height={10} style={{marginBottom:8}}/>
                <Skeleton width="80%" height={14}/>
              </div>
            ))}
          </div>
        </div>
      </div>
      <div style={{padding:22}}>
        <Skeleton width="80%" height={22} style={{marginBottom:8}}/>
        <Skeleton width="60%" height={14} style={{marginBottom:20}}/>
        <Skeleton width="100%" height={120} radius={12} style={{marginBottom:16}}/>
        <Skeleton width="100%" height={52} radius={12} style={{marginBottom:10}}/>
        <Skeleton width="100%" height={46} radius={12} style={{marginBottom:20}}/>
        <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:8}}>
          {[1,2,3,4].map((i)=><Skeleton key={i} height={36} radius={8}/>)}
        </div>
      </div>
    </div>
  );
}

export function SkeletonFeaturedCars({count=4}){
  return(
    <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(260px,1fr))',gap:20}}>
      {Array.from({length:count}).map((_,i)=><SkeletonCard key={i}/>)}
    </div>
  );
}