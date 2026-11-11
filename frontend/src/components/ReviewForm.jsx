import { useState } from 'react';
import { errorMessage, orderApi } from '../lib/api';
import { useToast } from '../context/ToastContext';
import './ReviewForm.css';

export default function ReviewForm({ orderId, productId }) {
  const { showToast } = useToast();
  const [open, setOpen] = useState(false);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [saving, setSaving] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      await orderApi.post('/api/reviews', { orderId, productId, rating, comment });
      showToast('Your verified review has been saved', 'success');
      setOpen(false);
      setComment('');
    } catch (err) {
      showToast(errorMessage(err, 'Could not save your review.'), 'error');
    } finally {
      setSaving(false);
    }
  };

  if (!open) return <button type="button" className="review-toggle" onClick={() => setOpen(true)}>Write or edit review</button>;
  return (
    <form className="review-form" onSubmit={submit}>
      <label>Rating<select value={rating} onChange={(event) => setRating(Number(event.target.value))}>{[5,4,3,2,1].map((value) => <option key={value} value={value}>{value} stars</option>)}</select></label>
      <label>Review<textarea minLength="10" maxLength="1000" required value={comment} onChange={(event) => setComment(event.target.value)} placeholder="How did the product fit and feel?" /></label>
      <div><button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save review'}</button><button type="button" className="btn btn-secondary" onClick={() => setOpen(false)}>Cancel</button></div>
    </form>
  );
}
