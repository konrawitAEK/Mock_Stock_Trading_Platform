export interface Stock {
  symbol: string;
  companyName: string;
  currentPrice: number;
  previousPrice: number;
  dailyChange: number;
  changePercent: number;
  sector: string;
  description: string;
}

export interface StockDetail extends Stock {
  heldQuantity: number;
  avgBuyPrice: number;
}

export interface HoldingItem {
  symbol: string;
  companyName: string;
  quantity: number;
  avgBuyPrice: number;
  currentPrice: number;
  marketValue: number;
  profitLoss: number;
}

export interface PortfolioResponse {
  cash: number;
  stockMarketValue: number;
  totalPortfolioValue: number;
  totalProfitLoss: number;
  holdings: HoldingItem[];
}

export interface Transaction {
  id: string;
  timestamp: string;
  type: 'BUY' | 'SELL';
  symbol: string;
  companyName: string;
  quantity: number;
  price: number;
  totalAmount: number;
}

export interface OrderRequest {
  symbol: string;
  quantity: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}
