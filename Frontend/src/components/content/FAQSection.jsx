import SectionHeading from '../common/SectionHeading.jsx'
import FAQItem from './FAQItem.jsx'
import questions from '../../data/reference-faqs.json'

function FAQSection() {
  return (
    <section className="content-section" id="faq" aria-labelledby="faq-title">
      <SectionHeading id="faq-title" title="Frequently Asked Questions" />
      <div className="faq-list">
        {questions.map((item) => (
          <FAQItem key={item.question} {...item} />
        ))}
      </div>
    </section>
  )
}

export default FAQSection