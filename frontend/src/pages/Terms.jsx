import './Policies.css';

export const TermsAndConditions = () => {
  return (
    <div className="policy-page">
      <h1>Terms & Conditions</h1>
      <p>Last updated: {new Date().toLocaleDateString()}</p>
      
      <h2>1. Introduction</h2>
      <p>Welcome to Axedrobe. These Terms and Conditions govern your use of our website and services. By accessing or using our website, you agree to be bound by these terms.</p>

      <h2>2. Products and Pricing</h2>
      <p>All products listed on the website, their descriptions, and their prices are each subject to change. Axedrobe reserves the right, at any time, to modify, suspend, or discontinue the sale of any product with or without notice.</p>

      <h2>3. Orders and Acceptance</h2>
      <p>Your receipt of an electronic or other form of order confirmation does not signify our acceptance of your order, nor does it constitute confirmation of our offer to sell. We reserve the right at any time after receipt of your order to accept or decline your order for any reason.</p>

      <h2>4. Intellectual Property</h2>
      <p>All content included on the site, such as text, graphics, logos, images, audio clips, video, data, music, software, and other material is owned or licensed property of Axedrobe or its software and content suppliers and is protected by copyright, trademark, patent, or other proprietary rights.</p>
    </div>
  );
};

export const TermsOfUse = () => {
  return (
    <div className="policy-page">
      <h1>Terms of Use</h1>
      <p>Last updated: {new Date().toLocaleDateString()}</p>
      
      <h2>1. Site Access</h2>
      <p>We grant you a limited, revocable, non-transferable, and non-exclusive license to access and use the Site by displaying it on your internet browser only for the purpose of shopping for personal items sold on the Site and not for any commercial use or use on behalf of any third party.</p>

      <h2>2. User Content</h2>
      <p>You agree not to post, transmit, or otherwise make available through or in connection with the Site any materials that are or may be: (a) threatening, harassing, degrading, hateful or intimidating; (b) defamatory; (c) fraudulent or tortious; (d) obscene, indecent, pornographic or otherwise objectionable.</p>

      <h2>3. Account Registration</h2>
      <p>You may be required to register an account. You agree to provide true, accurate, current, and complete information about yourself. You are responsible for maintaining the confidentiality of your account and password and for restricting access to your computer.</p>
    </div>
  );
};
