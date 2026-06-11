import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { NzMessageService } from "ng-zorro-antd/message";
import { OrderService } from "../../../core/services/order.service";
import { HoldingItem, TradeLimits } from "../../../core/models";

export type TradeMode = "BUY" | "SELL";

@Component({
  selector: "app-trading-drawer",
  templateUrl: "./trading-drawer.component.html",
  styleUrl: "./trading-drawer.component.scss",
})
export class TradingDrawerComponent implements OnChanges {
  @Input() visible = false;
  @Input() holding: HoldingItem | null = null;
  @Input() mode: TradeMode = "BUY";
  @Input() cash = 0;

  currentMode: TradeMode = "BUY";

  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() traded = new EventEmitter<void>();

  tradeForm: FormGroup;
  trading = false;
  tradeError: string | null = null;
  tradeLimits: TradeLimits | null = null;
  limitsLoading = false;

  constructor(
    private orderService: OrderService,
    private message: NzMessageService,
    private fb: FormBuilder,
  ) {
    this.tradeForm = this.fb.group({
      quantity: [
        1,
        [
          Validators.required,
          Validators.min(1),
          Validators.pattern("^[0-9]+$"),
        ],
      ],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    const becameVisible =
      changes["visible"]?.currentValue === true &&
      changes["visible"]?.previousValue !== true;
    const holdingChanged = changes["holding"] && this.visible;

    if (changes["visible"]?.currentValue === true) {
      this.currentMode = this.mode;
      this.tradeError = null;
      this.tradeForm.reset({ quantity: 1 });
    }

    if ((becameVisible || holdingChanged) && this.holding) {
      this.limitsLoading = true;
      this.orderService.getLimits(this.holding.symbol).subscribe({
        next: (limits) => {
          this.tradeLimits = limits;
          this.limitsLoading = false;
        },
        error: () => {
          this.limitsLoading = false;
        },
      });
    }

    if (changes["visible"]?.currentValue === false) {
      this.tradeLimits = null;
      this.limitsLoading = false;
    }
  }

  setMode(mode: TradeMode): void {
    this.currentMode = mode;
    this.tradeError = null;
    this.tradeForm.reset({ quantity: 1 });
  }

  get tradeTotal(): number {
    if (!this.holding) return 0;
    const qty = this.tradeForm.get("quantity")?.value as number;
    return (qty || 0) * this.holding.currentPrice;
  }

  onTrade(): void {
    if (this.tradeForm.invalid || !this.holding) return;
    this.trading = true;
    this.tradeError = null;
    const qty = this.tradeForm.value.quantity as number;
    const symbol = this.holding.symbol;
    const action$ =
      this.currentMode === "BUY"
        ? this.orderService.buy({ symbol, quantity: qty })
        : this.orderService.sell({ symbol, quantity: qty });

    action$.subscribe({
      next: () => {
        const verb = this.currentMode === "BUY" ? "Bought" : "Sold";
        this.message.success(`${verb} ${qty} shares of ${symbol}`);
        this.trading = false;
        this.close();
        this.traded.emit();
        this.limitsLoading = true;
        this.orderService.getLimits(symbol).subscribe({
          next: (limits) => {
            this.tradeLimits = limits;
            this.limitsLoading = false;
          },
          error: () => {
            this.limitsLoading = false;
          },
        });
      },
      error: (err) => {
        this.tradeError = err.message;
        this.trading = false;
      },
    });
  }

  close(): void {
    this.visibleChange.emit(false);
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
}
