import { useState, useRef, useEffect } from 'react';
import { Mail, Phone, MapPin, Send, Bot } from 'lucide-react';
import './Policies.css';

const ContactUs = () => {
  const [messages, setMessages] = useState([
    { text: "Hi there! I'm the Axedrobe help assistant. Ask me about delivery, returns, cancellations or tracking.", sender: 'bot' }
  ]);
  const [inputValue, setInputValue] = useState('');
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!inputValue.trim()) return;

    // Add user message
    const newMessages = [...messages, { text: inputValue, sender: 'user' }];
    setMessages(newMessages);
    setInputValue('');

    // Canned answers for the most common questions, matched by keyword
    setTimeout(() => {
      const lowerInput = inputValue.toLowerCase();
      let botResponse = "I'm sorry, I didn't quite catch that. You can ask me about shipping, returns, or tracking your order. For complex issues, please email our support team.";
      
      if (lowerInput.includes('shipping') || lowerInput.includes('delivery')) {
        botResponse = "Most orders arrive in 3-5 business days. Delivery is free on orders of Rs 999 or more, otherwise it's a flat Rs 79. We deliver across India.";
      } else if (lowerInput.includes('return') || lowerInput.includes('refund')) {
        botResponse = "We have a 14-day return policy for unused items with tags attached. Send us your order number using the form on this page and we'll arrange a pickup.";
      } else if (lowerInput.includes('track') || lowerInput.includes('where is my order')) {
        botResponse = "Open My Orders, or enter your order number (it starts with AXD) on the Track Orders page.";
      } else if (lowerInput.includes('cancel')) {
        botResponse = "You can cancel any order that hasn't shipped yet from the My Orders page. The cancellation is instant.";
      } else if (lowerInput.includes('hello') || lowerInput.includes('hi')) {
        botResponse = "Hello! How can I assist you with your Axedrobe shopping experience today?";
      }

      setMessages(prev => [...prev, { text: botResponse, sender: 'bot' }]);
    }, 800);
  };

  return (
    <div className="policy-page" style={{ maxWidth: '1000px' }}>
      <h1 style={{ marginBottom: '10px' }}>Contact Us</h1>
      <p style={{ textAlign: 'center', marginBottom: '50px' }}>We're here to help! Send us a message or ask our help assistant below.</p>

      <div className="contact-container">
        {/* Contact Details & Manual Form */}
        <div className="contact-info">
          <h2 style={{ marginTop: 0 }}>Get in Touch</h2>
          
          <div className="contact-details" style={{ marginTop: '30px', marginBottom: '40px' }}>
            <div className="contact-item">
              <MapPin size={24} />
              <div>
                <h4>Our Headquarters</h4>
                <p>Bengaluru, India</p>
              </div>
            </div>
            <div className="contact-item">
              <Phone size={24} />
              <div>
                <h4>Phone Support</h4>
                <p>8944905120<br/>Mon-Fri, 9am - 6pm IST</p>
              </div>
            </div>
            <div className="contact-item">
              <Mail size={24} />
              <div>
                <h4>Email Support</h4>
                <p>axedrobe@gmail.com<br/>We reply within 24 hours</p>
              </div>
            </div>
          </div>

          <h3 style={{ marginTop: '30px', marginBottom: '20px' }}>Send us a Message</h3>
          <form style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
            <input type="text" placeholder="Your Name" style={{ padding: '12px', borderRadius: 'var(--border-radius)', border: '1px solid var(--border-light)', background: 'var(--bg-primary)', color: 'var(--text-primary)' }} required />
            <input type="email" placeholder="Your Email" style={{ padding: '12px', borderRadius: 'var(--border-radius)', border: '1px solid var(--border-light)', background: 'var(--bg-primary)', color: 'var(--text-primary)' }} required />
            <textarea placeholder="How can we help?" rows="4" style={{ padding: '12px', borderRadius: 'var(--border-radius)', border: '1px solid var(--border-light)', background: 'var(--bg-primary)', color: 'var(--text-primary)', resize: 'vertical' }} required></textarea>
            <button type="submit" style={{ padding: '12px', background: 'transparent', color: 'var(--primary-color)', border: '2px solid var(--primary-color)', borderRadius: 'var(--border-radius)', fontWeight: 'bold', cursor: 'pointer' }}>
              Submit Request
            </button>
          </form>
        </div>

        {/* Keyword based help assistant */}
        <div className="help-chat-container" style={{ padding: 0, display: 'flex', flexDirection: 'column' }}>
          <div className="chat-window" style={{ border: 'none', height: '100%' }}>
            <div className="chat-header">
              <Bot size={24} />
              Axedrobe Help
            </div>
            <div className="chat-messages">
              {messages.map((msg, idx) => (
                <div key={idx} className={`message ${msg.sender}`}>
                  {msg.text}
                </div>
              ))}
              <div ref={messagesEndRef} />
            </div>
            <form className="chat-input" onSubmit={handleSendMessage}>
              <input 
                type="text" 
                placeholder="Ask me about shipping, returns..." 
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
              />
              <button type="submit">
                <Send size={18} />
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContactUs;
