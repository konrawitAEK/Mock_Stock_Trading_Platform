import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzMessageService } from 'ng-zorro-antd/message';
import { PortfolioService } from '../../core/services/portfolio.service';
import { MarketService } from '../../core/services/market.service';
import { PortfolioResponse } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    NzTableModule, NzCardModule, NzButtonModule,
    NzSpinModule, NzAlertModule, NzTagModule, NzIconModule, NzGridModule,
  ],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  portfolio: PortfolioResponse | null = null;
  loading = true;
  simulating = false;
  error: string | null = null;

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
      next: data => { this.portfolio = data; this.loading = false; },
      error: err => { this.error = err.message; this.loading = false; },
    });
  }

  simulate(): void {
    this.simulating = true;
    this.marketService.simulate().subscribe({
      next: () => {
        this.message.success('Market prices updated!');
        this.loadPortfolio();
        this.simulating = false;
      },
      error: err => { this.error = err.message; this.simulating = false; },
    });
  }

  formatCurrency(value: number): string {
    return '฿' + value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  isProfitable(val: number): boolean { return val >= 0; }
}
