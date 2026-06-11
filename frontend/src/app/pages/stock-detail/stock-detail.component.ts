import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzMessageService } from 'ng-zorro-antd/message';
import { StockService } from '../../core/services/stock.service';
import { OrderService } from '../../core/services/order.service';
import { StockDetail } from '../../core/models';

@Component({
  selector: 'app-stock-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule, ReactiveFormsModule,
    NzCardModule, NzDescriptionsModule, NzButtonModule, NzInputNumberModule,
    NzFormModule, NzTagModule, NzSpinModule, NzAlertModule, NzDividerModule, NzGridModule,
  ],
  templateUrl: './stock-detail.component.html',
})
export class StockDetailComponent implements OnInit {
  stock: StockDetail | null = null;
  loading = true;
  error: string | null = null;
  notFound = false;

  buyForm: FormGroup;
  sellForm: FormGroup;
  buying = false;
  selling = false;
  buyError: string | null = null;
  sellError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private stockService: StockService,
    private orderService: OrderService,
    private message: NzMessageService,
    private fb: FormBuilder,
  ) {
    this.buyForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]+$')]],
    });
    this.sellForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]+$')]],
    });
  }

  ngOnInit(): void {
    const symbol = this.route.snapshot.paramMap.get('symbol')!;
    this.loadStock(symbol);
  }

  loadStock(symbol: string): void {
    this.loading = true;
    this.error = null;
    this.stockService.getBySymbol(symbol).subscribe({
      next: data => { this.stock = data; this.loading = false; },
      error: err => {
        if (err.message?.toLowerCase().includes('not found')) {
          this.notFound = true;
        } else {
          this.error = err.message;
        }
        this.loading = false;
      },
    });
  }

  get buyCost(): number {
    const qty = this.buyForm.get('quantity')?.value as number;
    return this.stock ? (qty || 0) * this.stock.currentPrice : 0;
  }

  get sellProceeds(): number {
    const qty = this.sellForm.get('quantity')?.value as number;
    return this.stock ? (qty || 0) * this.stock.currentPrice : 0;
  }

  onBuy(): void {
    if (this.buyForm.invalid || !this.stock) return;
    this.buying = true;
    this.buyError = null;
    const qty = this.buyForm.value.quantity as number;
    const symbol = this.stock.symbol;
    this.orderService.buy({ symbol, quantity: qty }).subscribe({
      next: () => {
        this.message.success(`Bought ${qty} shares of ${symbol}`);
        this.buying = false;
        this.buyForm.reset({ quantity: 1 });
        this.loadStock(symbol);
      },
      error: err => { this.buyError = err.message; this.buying = false; },
    });
  }

  onSell(): void {
    if (this.sellForm.invalid || !this.stock) return;
    this.selling = true;
    this.sellError = null;
    const qty = this.sellForm.value.quantity as number;
    const symbol = this.stock.symbol;
    this.orderService.sell({ symbol, quantity: qty }).subscribe({
      next: () => {
        this.message.success(`Sold ${qty} shares of ${symbol}`);
        this.selling = false;
        this.sellForm.reset({ quantity: 1 });
        this.loadStock(symbol);
      },
      error: err => { this.sellError = err.message; this.selling = false; },
    });
  }

  formatCurrency(value: number): string {
    return '฿' + value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatPercent(value: number): string {
    return (value >= 0 ? '+' : '') + value.toFixed(2) + '%';
  }

  isPositive(val: number): boolean { return val >= 0; }
}
