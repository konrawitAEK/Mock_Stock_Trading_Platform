import { Component, OnInit } from '@angular/core';
import { TransactionService } from '../../core/services/transaction.service';
import { Transaction } from '../../core/models';

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.scss',
})
export class TransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  loading = true;
  error: string | null = null;

  filterSymbol = '';
  filterType: 'ALL' | 'BUY' | 'SELL' = 'ALL';
  filterStartDate: Date | null = null;
  filterEndDate: Date | null = null;
  pageSize = 20;
  pageSizeOptions = [10, 20, 50, 100];

  constructor(private transactionService: TransactionService) {}

  ngOnInit(): void {
    this.transactionService.getAll().subscribe({
      next: data => { this.transactions = data; this.loading = false; },
      error: err => { this.error = err.message; this.loading = false; },
    });
  }

  get filteredTransactions(): Transaction[] {
    return this.transactions.filter(t => {
      const symbolMatch = !this.filterSymbol || t.symbol.toUpperCase().includes(this.filterSymbol.toUpperCase());
      const typeMatch = this.filterType === 'ALL' || t.type === this.filterType;

      const txDate = new Date(t.timestamp);
      const startMatch = !this.filterStartDate || txDate >= this.dayStart(this.filterStartDate);
      const endMatch = !this.filterEndDate || txDate <= this.dayEnd(this.filterEndDate);

      return symbolMatch && typeMatch && startMatch && endMatch;
    });
  }

  onStartDateChange(date: Date | null): void {
    this.filterStartDate = date;
    if (!date) return;

    const isToday = this.dayStart(date).getTime() === this.dayStart(new Date()).getTime();
    if (isToday) {
      this.filterEndDate = new Date();
    } else if (this.filterEndDate && this.dayStart(this.filterEndDate) < this.dayStart(date)) {
      this.filterEndDate = null;
    }
  }

  disabledStartDate = (current: Date): boolean => {
    return this.dayStart(current) > this.dayStart(new Date());
  };

  disabledEndDate = (current: Date): boolean => {
    if (this.dayStart(current) > this.dayStart(new Date())) return true;
    if (this.filterStartDate && this.dayStart(current) < this.dayStart(this.filterStartDate)) return true;
    return false;
  };

  clearFilters(): void {
    this.filterSymbol = '';
    this.filterType = 'ALL';
    this.filterStartDate = null;
    this.filterEndDate = null;
  }

  get hasActiveFilter(): boolean {
    return !!this.filterSymbol || this.filterType !== 'ALL' || !!this.filterStartDate || !!this.filterEndDate;
  }

  formatCurrency(value: number): string {
    return '฿' + value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('en-US', {
      year: 'numeric', month: 'short', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    });
  }

  private dayStart(date: Date): Date {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  private dayEnd(date: Date): Date {
    const d = new Date(date);
    d.setHours(23, 59, 59, 999);
    return d;
  }
}
