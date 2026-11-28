import markets from './markets.full.json'

const priorityMarketNames = [
  'RAJSHREE DAY',
  'RAJSHREE NIGHT',
  'KALYAN MORNING',
  'KALYAN',
]
const priorityOrder = new Map(priorityMarketNames.map((name, index) => [name, index]))
const newRajshreeMarkets = [
  { name: 'RAJSHREE DAY', openTime: '03:15 PM', closeTime: '05:15 PM' },
  { name: 'RAJSHREE NIGHT', openTime: '09:35 PM', closeTime: '11:45 PM' },
]

const orderedMarkets = [...newRajshreeMarkets, ...markets]
  .map((market, originalIndex) => ({ market, originalIndex }))
  .sort((left, right) => {
    const leftOrder = priorityOrder.get(left.market.name) ?? Number.MAX_SAFE_INTEGER
    const rightOrder = priorityOrder.get(right.market.name) ?? Number.MAX_SAFE_INTEGER
    return leftOrder - rightOrder || left.originalIndex - right.originalIndex
  })
  .map(({ market }) => market)

export const featuredMarketNames = new Set([
  'RAJSHREE DAY',
  'RAJSHREE NIGHT',
])

export default orderedMarkets