import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { StockService } from '../../../core/services/stock.service';
import { OrderService } from '../../../core/services/order.service';
import { PortfolioService } from '../../../core/services/portfolio.service';
import { StockDetail, TradeLimits } from '../../../core/models';

type TradeMode = 'BUY' | 'SELL';

@Component({
  selector: 'app-stock-detail-drawer',
  templateUrl: './stock-detail-drawer.component.html',
  styleUrl: './stock-detail-drawer.component.scss',
})
export class StockDetailDrawerComponent implements OnChanges {
  @Input() visible = false;
  @Input() symbol: string | null = null;

  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() traded = new EventEmitter<void>();

  stock: StockDetail | null = null;
  cash = 0;
  tradeLimits: TradeLimits | null = null;
  loading = false;
  limitsLoading = false;
  error: string | null = null;
  notFound = false;
  mode: TradeMode = 'BUY';

  buyForm: FormGroup;
  sellForm: FormGroup;
  buying = false;
  selling = false;
  buyError: string | null = null;
  sellError: string | null = null;

  constructor(
    private stockService: StockService,
    private orderService: OrderService,
    private portfolioService: PortfolioService,
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

  ngOnChanges(changes: SimpleChanges): void {
    const becameVisible = changes['visible']?.currentValue === true && changes['visible']?.previousValue !== true;
    const symbolChanged = changes['symbol'] && this.visible;

    if ((becameVisible || symbolChanged) && this.symbol) {
      this.loadStock(this.symbol);
    }

    if (becameVisible) {
      this.portfolioService.get().subscribe({ next: p => { this.cash = p.cash; } });
    }

    if ((becameVisible || symbolChanged) && this.symbol) {
      this.limitsLoading = true;
      this.orderService.getLimits(this.symbol).subscribe({
        next: limits => { this.tradeLimits = limits; this.limitsLoading = false; },
        error: () => { this.limitsLoading = false; },
      });
    }

    if (changes['visible']?.currentValue === false) {
      this.stock = null;
      this.tradeLimits = null;
      this.limitsLoading = false;
      this.error = null;
      this.notFound = false;
      this.buyError = null;
      this.sellError = null;
      this.mode = 'BUY';
      this.buyForm.reset({ quantity: 1 });
      this.sellForm.reset({ quantity: 1 });
    }
  }

  loadStock(symbol: string): void {
    this.loading = true;
    this.error = null;
    this.notFound = false;
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

  setMode(mode: TradeMode): void {
    this.mode = mode;
    this.buyError = null;
    this.sellError = null;
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
        this.refreshCash();
        this.traded.emit();
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
        this.refreshCash();
        this.traded.emit();
      },
      error: err => { this.sellError = err.message; this.selling = false; },
    });
  }

  refreshCash(): void {
    this.portfolioService.get().subscribe({ next: p => { this.cash = p.cash; } });
    if (this.symbol) {
      this.limitsLoading = true;
      this.orderService.getLimits(this.symbol).subscribe({
        next: limits => { this.tradeLimits = limits; this.limitsLoading = false; },
        error: () => { this.limitsLoading = false; },
      });
    }
  }

  close(): void {
    this.visibleChange.emit(false);
  }

  formatCurrency(value: number): string {
    return '฿' + value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatPercent(value: number): string {
    return (value >= 0 ? '+' : '') + value.toFixed(2) + '%';
  }

  isPositive(val: number): boolean { return val >= 0; }
}
