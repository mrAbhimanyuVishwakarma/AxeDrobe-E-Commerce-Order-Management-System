import { useState } from 'react';
import { BRAND_NAME, FREE_SHIPPING_FROM, SHIPPING_FEE } from '../config';
import { formatPrice } from '../lib/format';
import './FAQSection.css';

const faqs = [
  {
    question: `How do I sign in to ${BRAND_NAME}?`,
    answer: 'Sign in with Google, with your email or mobile number and a one-time code, or with your email and password. Signing in with a code for the first time creates your account automatically.',
  },
  {
    question: 'What payment methods do you accept?',
    answer: 'All orders are currently Cash on Delivery. Pay in cash or by UPI to the delivery partner when your order arrives.',
  },
  {
    question: 'How much does delivery cost?',
    answer: `Delivery is free on orders of ${formatPrice(FREE_SHIPPING_FROM)} or more. Smaller orders have a flat ${formatPrice(SHIPPING_FEE)} delivery fee. Most orders arrive within 3 to 5 business days.`,
  },
  {
    question: 'How do I track my order?',
    answer: 'Open My Orders, or go to Track Order and enter the order number from your confirmation (it starts with AXD). You will see each step from confirmation to delivery.',
  },
  {
    question: 'Can I cancel my order?',
    answer: 'Yes. Go to My Orders and tap Cancel on any order that has not shipped yet. The items go straight back into stock and you will get a confirmation email.',
  },
  {
    question: 'What is your return policy?',
    answer: 'You can return unused items with tags within 14 days of delivery. Contact our support team with your order number and we will arrange a pickup.',
  },
  {
    question: 'How do I find my size?',
    answer: 'Every product page lists the available sizes. Our clothing is true to size; if you are between sizes, we recommend going one size up for a relaxed fit.',
  },
  {
    question: 'I forgot my password. What should I do?',
    answer: 'Choose "Forgot password" on the sign-in page. We will send a code to your email or mobile number so you can set a new password. You can also just sign in with a code.',
  },
];

const FAQSection = () => {
  const [activeIndex, setActiveIndex] = useState(null);

  const faqSchema = {
    '@context': 'https://schema.org',
    '@type': 'FAQPage',
    mainEntity: faqs.map((faq) => ({
      '@type': 'Question',
      name: faq.question,
      acceptedAnswer: { '@type': 'Answer', text: faq.answer },
    })),
  };

  return (
    <section className="faq-section container">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(faqSchema) }} />
      <div className="faq-header">
        <h2>Frequently Asked Questions</h2>
        <p>Everything you need to know about shopping with {BRAND_NAME}.</p>
      </div>
      <div className="faq-list">
        {faqs.map((faq, index) => (
          <div key={faq.question} className={`faq-item ${activeIndex === index ? 'active' : ''}`}>
            <button
              type="button"
              className="faq-question"
              aria-expanded={activeIndex === index}
              onClick={() => setActiveIndex(activeIndex === index ? null : index)}
            >
              <h3>{faq.question}</h3>
              <span className="faq-icon" aria-hidden="true">{activeIndex === index ? '-' : '+'}</span>
            </button>
            <div className="faq-answer">
              <p>{faq.answer}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};

export default FAQSection;
