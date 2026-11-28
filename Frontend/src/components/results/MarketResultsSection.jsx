import { useEffect, useState } from 'react'
import MarketCard from './MarketCard.jsx'
import { featuredMarketNames } from '../../data/markets.js'

const scheduledHighlightMarketNames = new Set([
  'KALYAN',
  'KALYAN NIGHT',
  'MILAN DAY',
  'MILAN NIGHT',
  'MAIN BAZAR',
])

function normalizeMarketName(name) {
  return name.trim().replace(/\s+/g, ' ').toUpperCase()
}

function getIndiaDateTime(date) {
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone: 'Asia/Kolkata',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(date)
  const values = Object.fromEntries(parts.map(({ type, value }) => [type, value]))

  return {
    date: `${values.year}-${values.month}-${values.day}`,
    minutes: Number(values.hour) * 60 + Number(values.minute),
  }
}

function getScheduledMinutes(time) {
  const twentyFourHourTime = /^(\d{1,2}):(\d{2})$/.exec(time || '')
  if (twentyFourHourTime) {
    const hours = Number(twentyFourHourTime[1])
    const minutes = Number(twentyFourHourTime[2])
    return hours <= 23 && minutes <= 59 ? hours * 60 + minutes : null
  }

  const match = /^(\d{1,2}):(\d{2})\s*(AM|PM)$/i.exec(time || '')
  if (!match) return null

  const hours = Number(match[1])
  const minutes = Number(match[2])
  if (hours < 1 || hours > 12 || minutes > 59) return null

  const hourOfDay = (hours % 12) + (match[3].toUpperCase() === 'PM' ? 12 : 0)
  return hourOfDay * 60 + minutes
}

function formatTimeForDisplay(time) {
  const totalMinutes = getScheduledMinutes(time)
  if (totalMinutes == null) return time

  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60
  const displayHours = hours % 12 || 12
  const displayMinutes = String(minutes).padStart(2, '0')
  const meridiem = hours < 12 ? 'AM' : 'PM'
  return `${displayHours}:${displayMinutes} ${meridiem}`
}

function isMarketHighlighted(market, marketKey, result, indiaDateTime) {
  if (featuredMarketNames.has(marketKey)) return true
  if (!scheduledHighlightMarketNames.has(marketKey)) return false

  const openResultArrivedToday = result?.resultDate === indiaDateTime.date
    && result.apiOpenPana != null
    && result.apiOpenDigit != null
  const scheduledMinutes = getScheduledMinutes(market.openTime)

  return openResultArrivedToday
    || (scheduledMinutes != null && indiaDateTime.minutes >= scheduledMinutes - 30)
}

function MarketResultsSection({ markets, results, marketTimings, rajshreeResults }) {
  const [currentTime, setCurrentTime] = useState(() => new Date())
  useEffect(() => {
    const timer = window.setInterval(() => setCurrentTime(new Date()), 10_000)
    return () => window.clearInterval(timer)
  }, [])

  const indiaDateTime = getIndiaDateTime(currentTime)
  const resultByMarket = new Map(
    results.map((result) => [normalizeMarketName(result.marketName), result]),
  )
  const externalResultsByMarket = new Map([
    ['RAJSHREE DAY', rajshreeResults.day],
    ['RAJSHREE NIGHT', rajshreeResults.night],
  ])
  const marketNames = new Set(markets.map((market) => normalizeMarketName(market.name)))
  const cards = markets.map((market) => {
    const marketKey = normalizeMarketName(market.name)
    const externalState = externalResultsByMarket.get(marketKey)
    const latestResult = externalState
      ? externalState.data
      : resultByMarket.get(marketKey)
    const timingValues = marketKey === 'RAJSHREE DAY'
      ? [marketTimings?.dayOpenTime, marketTimings?.dayCloseTime]
      : marketKey === 'RAJSHREE NIGHT'
        ? [marketTimings?.nightOpenTime, marketTimings?.nightCloseTime]
        : []
    const marketWithCurrentTimings = timingValues.length
      ? {
          ...market,
          openTime: formatTimeForDisplay(timingValues[0]) || market.openTime,
          closeTime: formatTimeForDisplay(timingValues[1]) || market.closeTime,
        }
      : market

    return {
      ...marketWithCurrentTimings,
      result: latestResult?.formattedResult
        || (externalState
          ? externalState.loading
            ? 'Loading result...'
            : externalState.error
              ? 'Result unavailable'
              : 'Not published yet'
          : 'Awaiting result'),
      resultDate: latestResult?.resultDate,
      highlighted: isMarketHighlighted(
        marketWithCurrentTimings,
        marketKey,
        latestResult,
        indiaDateTime,
      ),
    }
  })

  for (const result of results) {
    if (!marketNames.has(normalizeMarketName(result.marketName))) {
      cards.push({
        name: result.marketName,
        result: result.formattedResult || 'Awaiting result',
        resultDate: result.resultDate,
        highlighted: false,
      })
    }
  }

  return (
    <section className="market-results-section" id="markets" aria-label="Market results">
      <div className="market-grid">
        {cards.map((market, index) => (
          <MarketCard key={`${market.name}-${index}`} {...market} />
        ))}
      </div>
    </section>
  )
}

export default MarketResultsSection