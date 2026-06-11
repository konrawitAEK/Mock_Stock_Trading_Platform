import { Component, OnInit } from "@angular/core";
import { NzMessageService } from "ng-zorro-antd/message";
import { PortfolioService } from "../../core/services/portfolio.service";
import { MarketService } from "../../core/services/market.service";
import { HoldingItem, PortfolioResponse } from "../../core/models";
import { TradeMode } from "../../shared/trading-drawer/trading-drawer.component";

@Component({
  selector: "app-dashboard",
  templateUrl: "./dashboard.component.html",
  styleUrl: "./dashboard.component.scss",
})
export class DashboardComponent implements OnInit {
  portfolio: PortfolioResponse | null = null;
  loading = true;
  simulating = false;
  error: string | null = null;

  drawerVisible = false;
  drawerMode: TradeMode = "BUY";
  selectedHolding: HoldingItem | null = null;

  constructor(
    private portfolioService: PortfolioService,
    private marketService: MarketService,
    private message: NzMessageService,
  ) {}

  ngOnInit(): void {
    this.loadPortfolio();
  }

  loadPortfolio(): void {
    this.loading = true;
    this.error = null;
    this.portfolioService.get().subscribe({
      next: (data) => {
        this.portfolio = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      },
    });
  }

  simulate(): void {
    this.simulating = true;
    this.marketService.simulate().subscribe({
      next: () => {
        this.message.success("Market prices updated!");
        this.loadPortfolio();
        this.simulating = false;
      },
      error: (err) => {
        this.error = err.message;
        this.simulating = false;
      },
    });
  }

  openDrawer(holding: HoldingItem, mode: TradeMode): void {
    this.selectedHolding = holding;
    this.drawerMode = mode;
    this.drawerVisible = true;
  }

  onTraded(): void {
    this.loadPortfolio();
  }

  formatCurrency(value: number): string {
    return (
      "฿" +
      value.toLocaleString("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      })
    );
  }

  isProfitable(val: number): boolean {
    return val >= 0;
  }
}
