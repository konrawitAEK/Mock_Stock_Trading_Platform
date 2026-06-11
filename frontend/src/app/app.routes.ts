import { Routes } from '@angular/router';
import { LayoutComponent } from './shared/layout/layout.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { StockListComponent } from './pages/stock-list/stock-list.component';
import { TransactionsComponent } from './pages/transactions/transactions.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', component: DashboardComponent },
      { path: 'stocks', component: StockListComponent },
      { path: 'transactions', component: TransactionsComponent },
      { path: '**', redirectTo: '' },
    ],
  },
];
