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
      return symbolMatch && typeMatch;
    });
  }

  clearFilters(): void {
    this.filterSymbol = '';
    this.filterType = 'ALL';
  }

  get hasActiveFilter(): boolean {
    return !!this.filterSymbol || this.filterType !== 'ALL';
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
}
