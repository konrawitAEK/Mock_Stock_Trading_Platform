import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';
import { OrderService } from '../../core/services/order.service';
import { HoldingItem } from '../../core/models';

export type TradeMode = 'BUY' | 'SELL';

@Component({
  selector: 'app-trading-drawer',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    NzDrawerModule, NzButtonModule, NzInputNumberModule,
    NzFormModule, NzDividerModule, NzAlertModule, NzIconModule,
  ],
  templateUrl: './trading-drawer.component.html',
  styleUrl: './trading-drawer.component.scss',
})
export class TradingDrawerComponent implements OnChanges {
  @Input() visible = false;
  @Input() holding: HoldingItem | null = null;
  @Input() mode: TradeMode = 'BUY';

  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() traded = new EventEmitter<void>();

  tradeForm: FormGroup;
  trading = false;
  tradeError: string | null = null;

  constructor(
    private orderService: OrderService,
    private message: NzMessageService,
    private fb: FormBuilder,
  ) {
    this.tradeForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1), Validators.pattern('^[0-9]+$')]],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue === true) {
      this.tradeError = null;
      this.tradeForm.reset({ quantity: 1 });
    }
  }

  setMode(mode: TradeMode): void {
    this.mode = mode;
    this.tradeError = null;
    this.tradeForm.reset({ quantity: 1 });
  }

  get tradeTotal(): number {
    if (!this.holding) return 0;
    const qty = this.tradeForm.get('quantity')?.value as number;
    return (qty || 0) * this.holding.currentPrice;
  }

  get maxSellQty(): number {
    return this.holding?.quantity ?? 0;
  }

  onTrade(): void {
    if (this.tradeForm.invalid || !this.holding) return;
    this.trading = true;
    this.tradeError = null;
    const qty = this.tradeForm.value.quantity as number;
    const symbol = this.holding.symbol;
    const action$ = this.mode === 'BUY'
      ? this.orderService.buy({ symbol, quantity: qty })
      : this.orderService.sell({ symbol, quantity: qty });

    action$.subscribe({
      next: () => {
        const verb = this.mode === 'BUY' ? 'Bought' : 'Sold';
        this.message.success(`${verb} ${qty} shares of ${symbol}`);
        this.trading = false;
        this.close();
        this.traded.emit();
      },
      error: err => { this.tradeError = err.message; this.trading = false; },
    });
  }

  close(): void {
    this.visibleChange.emit(false);
  }

  formatCurrency(value: number): string {
    return '฿' + value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
