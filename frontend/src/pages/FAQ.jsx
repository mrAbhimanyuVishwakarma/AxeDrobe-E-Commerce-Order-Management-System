import { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
import './Policies.css';

const faqs = [
  {
    question: "How long does shipping take?",
    answer: "Most orders arrive within 3-5 business days. Delivery is free on orders of Rs 999 or more."
  },
  {
    question: "What is your return policy?",
    answer: "We offer a 14-day return window from the date of delivery for all unworn, unwashed items with tags attached."
  },
  {
    question: "Do you ship internationally?",
    answer: "Not yet. We currently deliver to all serviceable PIN codes across India."
  },
  {
    question: "How can I track my order?",
    answer: "Open My Orders, or enter your order number (it starts with AXD) on the Track Orders page to see every step from confirmation to delivery."
  },
  {
    question: "Are your sizes true to size?",
    answer: "Our clothing runs true to size. Every product page lists the available sizes; if you are between sizes, go one size up for a relaxed fit."
  }
];

const FAQ = () => {
  const [openIndex, setOpenIndex] = useState(null);

  const toggleFAQ = (index) => {
    setOpenIndex(openIndex === index ? null : index);
  };

  return (
    <div className="policy-page">
      <h1>Frequently Asked Questions</h1>
      <p style={{ textAlign: 'center', marginBottom: '40px' }}>Find answers to our most common questions below.</p>
      
      <div className="faq-list">
        {faqs.map((faq, index) => (
          <div key={index} className={`faq-item ${openIndex === index ? 'open' : ''}`}>
            <div className="faq-question" onClick={() => toggleFAQ(index)}>
              {faq.question}
              {openIndex === index ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
            </div>
            <div className="faq-answer">
              {faq.answer}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default FAQ;
