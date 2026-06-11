import { Component, OnInit } from "@angular/core";
import { NzMessageService } from "ng-zorro-antd/message";
import { StockService } from "../../core/services/stock.service";
import { MarketService } from "../../core/services/market.service";
import { Stock } from "../../core/models";

@Component({
  selector: "app-stock-list",
  templateUrl: "./stock-list.component.html",
  styleUrl: "./stock-list.component.scss",
})
export class StockListComponent implements OnInit {
  stocks: Stock[] = [];
  filteredStocks: Stock[] = [];
  loading = true;
  simulating = false;
  error: string | null = null;
  searchQuery = "";

  drawerVisible = false;
  selectedSymbol: string | null = null;

  constructor(
    private stockService: StockService,
    private marketService: MarketService,
    private message: NzMessageService,
  ) {}

  ngOnInit(): void {
    this.loadStocks();
  }

  loadStocks(): void {
    this.loading = true;
    this.stockService.getAll().subscribe({
      next: (data) => {
        this.stocks = data;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      },
    });
  }

  applyFilter(): void {
    const q = this.searchQuery.toLowerCase().trim();
    this.filteredStocks = this.stocks.filter(
      (s) =>
        s.symbol.toLowerCase().includes(q) ||
        s.companyName.toLowerCase().includes(q),
    );
  }

  onSearch(): void {
    this.applyFilter();
  }

  openDrawer(symbol: string): void {
    this.selectedSymbol = symbol;
    this.drawerVisible = true;
  }

  simulate(): void {
    this.simulating = true;
    this.marketService.simulate().subscribe({
      next: (updated) => {
        this.stocks = updated;
        this.applyFilter();
        this.message.success("Market prices updated!");
        this.simulating = false;
      },
      error: (err) => {
        this.error = err.message;
        this.simulating = false;
      },
    });
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

  formatPercent(value: number): string {
    return (value >= 0 ? "+" : "") + value.toFixed(2) + "%";
  }

  isPositive(val: number): boolean {
    return val >= 0;
  }
}
