import SectionHeading from '../common/SectionHeading.jsx'
import referenceContent from '../../data/reference-content.json'

function GuideArticlesSection() {
  return (
    <section className="content-section" aria-labelledby="guides-title">
      <SectionHeading id="guides-title" title="Dpboss Matka Guide" />
      <div className="article-list">
        {referenceContent.map((section, index) => (
          <article className="article-card" key={`${section.title}-${index}`}>
            <h3>{section.title}</h3>
            {section.paragraphs.map((paragraph, paragraphIndex) => (
              <p key={`${section.title}-${paragraphIndex}`}>{paragraph}</p>
            ))}
          </article>
        ))}
      </div>
    </section>
  )
}

export default GuideArticlesSection