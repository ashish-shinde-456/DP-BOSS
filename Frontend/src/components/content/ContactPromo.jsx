import SectionHeading from '../common/SectionHeading.jsx'

function ContactPromo() {
  return (
    <section className="content-section contact-promo" aria-labelledby="contact-promo-title">
      <SectionHeading id="contact-promo-title" title="Add Your Game" description="Email Us to submit a game listing." />
      <a href="mailto:support@dpboss.net">support@dpboss.net</a>
    </section>
  )
}

export default ContactPromo