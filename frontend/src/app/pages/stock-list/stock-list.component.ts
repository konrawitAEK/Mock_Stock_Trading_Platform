import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { NzTableModule } from "ng-zorro-antd/table";
import { NzInputModule } from "ng-zorro-antd/input";
import { NzButtonModule } from "ng-zorro-antd/button";
import { NzTagModule } from "ng-zorro-antd/tag";
import { NzSpinModule } from "ng-zorro-antd/spin";
import { NzAlertModule } from "ng-zorro-antd/alert";
import { NzIconModule } from "ng-zorro-antd/icon";
import { NzCardModule } from "ng-zorro-antd/card";
import { NzMessageService } from "ng-zorro-antd/message";
import { StockService } from "../../core/services/stock.service";
import { MarketService } from "../../core/services/market.service";
import { Stock } from "../../core/models";
import { StockDetailDrawerComponent } from "../../shared/stock-detail-drawer/stock-detail-drawer.component";

@Component({
  selector: "app-stock-list",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NzTableModule,
    NzInputModule,
    NzButtonModule,
    NzTagModule,
    NzSpinModule,
    NzAlertModule,
    NzIconModule,
    NzCardModule,
    StockDetailDrawerComponent,
  ],
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
