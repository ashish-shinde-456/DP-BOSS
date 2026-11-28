import { useEffect, useState } from 'react'
import ContactPromo from '../components/content/ContactPromo.jsx'
import FAQSection from '../components/content/FAQSection.jsx'
import GuideArticlesSection from '../components/content/GuideArticlesSection.jsx'
import GameZoneSection from '../components/charts/GameZoneSection.jsx'
import WeeklyChartsSection from '../components/charts/WeeklyChartsSection.jsx'
import AppDownloadBanner from '../components/common/AppDownloadBanner.jsx'
import SiteFooter from '../components/layout/SiteFooter.jsx'
import SiteHeader from '../components/layout/SiteHeader.jsx'
// Temporarily disabled; uncomment this import and its JSX below to restore it.
// import LiveJackpotSection from '../components/results/LiveJackpotSection.jsx'
import LuckyNumbersSection from '../components/results/LuckyNumbersSection.jsx'
import MarketResultsSection from '../components/results/MarketResultsSection.jsx'
import markets from '../data/markets.js'
import { goldenAnk as fallbackGoldenAnk } from '../data/results.js'
import {
  fetchGoldenAnk,
  fetchMarketResults,
  fetchMarketTimings,
  fetchRajshreeResult,
} from '../services/homepageApi.js'

function HomePage() {
  const [marketResults, setMarketResults] = useState([])
  const [marketError, setMarketError] = useState('')
  const [isLoadingMarkets, setIsLoadingMarkets] = useState(true)
  const [goldenAnk, setGoldenAnk] = useState(fallbackGoldenAnk)
  const [goldenAnkError, setGoldenAnkError] = useState('')
  const [marketTimings, setMarketTimings] = useState(null)
  const [marketTimingsError, setMarketTimingsError] = useState('')
  const [rajshreeResults, setRajshreeResults] = useState({
    day: { data: null, error: '', loading: true },
    night: { data: null, error: '', loading: true },
  })

  useEffect(() => {
    let isMounted = true
    let marketRequestInProgress = false
    let timingsRequestInProgress = false
    let goldenAnkRequestInProgress = false
    const rajshreeRequestsInProgress = { day: false, night: false }

    const loadMarketResults = async () => {
      if (marketRequestInProgress) return
      marketRequestInProgress = true

      try {
        const data = await fetchMarketResults()
        if (isMounted) {
          setMarketResults(data)
          setMarketError('')
        }
      } catch (error) {
        if (isMounted) {
          setMarketError(error instanceof Error ? error.message : 'Unable to load market results.')
        }
      } finally {
        marketRequestInProgress = false
        if (isMounted) setIsLoadingMarkets(false)
      }
    }

    const loadRajshreeResult = async (key, marketSlug) => {
      if (rajshreeRequestsInProgress[key]) return
      rajshreeRequestsInProgress[key] = true

      try {
        const data = await fetchRajshreeResult(marketSlug)
        if (isMounted) {
          setRajshreeResults((current) => ({
            ...current,
            [key]: { data, error: '', loading: false },
          }))
        }
      } catch {
        if (isMounted) {
          setRajshreeResults((current) => ({
            ...current,
            [key]: { data: null, error: 'Unable to load this result.', loading: false },
          }))
        }
      } finally {
        rajshreeRequestsInProgress[key] = false
      }
    }

    const loadMarketTimings = async () => {
      if (timingsRequestInProgress) return
      timingsRequestInProgress = true

      try {
        const data = await fetchMarketTimings()
        if (isMounted) {
          setMarketTimings(data)
          setMarketTimingsError('')
        }
      } catch (error) {
        if (isMounted) {
          setMarketTimingsError(
            error instanceof Error ? error.message : 'Unable to load market timings.',
          )
        }
      } finally {
        timingsRequestInProgress = false
      }
    }

    const loadGoldenAnk = async () => {
      if (goldenAnkRequestInProgress) return
      goldenAnkRequestInProgress = true

      try {
        const data = await fetchGoldenAnk()
        if (isMounted) {
          if (data) setGoldenAnk(data.goldenAnk)
          setGoldenAnkError('')
        }
      } catch (error) {
        if (isMounted) {
          setGoldenAnkError(
            error instanceof Error ? error.message : 'Unable to load Golden Ank.',
          )
        }
      } finally {
        goldenAnkRequestInProgress = false
      }
    }

    const refreshResults = () => {
      loadMarketResults()
      loadMarketTimings()
      loadGoldenAnk()
      loadRajshreeResult('day', 'rajshree-day')
      loadRajshreeResult('night', 'rajshree-night')
    }

    refreshResults()
    const refreshTimer = window.setInterval(refreshResults, 10_000)

    return () => {
      isMounted = false
      window.clearInterval(refreshTimer)
    }
  }, [])

  return (
    <div className="page-shell" id="top">
      <SiteHeader />
      <section className="page-intro">
        <h1>Satta Matka Dpboss.net Kalyan Matka Result</h1>
        <p>Dpboss boston is the No. 1 Matka Sites welcomes you full-heartedly. Here below you can find the perfect guess by the top guesser along with the Fast Matka Result too.</p>
        <p>Satta Matka is a daily numbers-based game followed by millions in India. DPBOSS brings together results, market charts, and reference guides in one place.</p>
        <p>Find fast result updates, market timings, and historical chart sections for Kalyan, Milan, Rajshree, and other listed markets.</p>
      </section>
      <main className="page-content">
        <LuckyNumbersSection goldenAnk={goldenAnk} />
        {/*
        <LiveJackpotSection />
        */}
        <AppDownloadBanner />
        {isLoadingMarkets && <p role="status">Loading market results...</p>}
        {marketError && <p role="alert">Could not refresh market results: {marketError}</p>}
        {marketTimingsError && (
          <p role="alert">Could not refresh Rajshree market timings: {marketTimingsError}</p>
        )}
        {goldenAnkError && <p role="alert">Could not refresh Golden Ank: {goldenAnkError}</p>}
        <MarketResultsSection
          markets={markets}
          results={marketResults}
          marketTimings={marketTimings}
          rajshreeResults={rajshreeResults}
        />
        <WeeklyChartsSection />
        <GameZoneSection />
        <GuideArticlesSection />
        <FAQSection />
        <ContactPromo />
      </main>
      <SiteFooter />
    </div>
  )
}

export default HomePage