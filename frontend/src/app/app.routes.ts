import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./shared/layout/layout.component').then(m => m.LayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'stocks',
        loadComponent: () =>
          import('./pages/stock-list/stock-list.component').then(m => m.StockListComponent),
      },
      {
        path: 'stocks/:symbol',
        loadComponent: () =>
          import('./pages/stock-detail/stock-detail.component').then(m => m.StockDetailComponent),
      },
      {
        path: 'transactions',
        loadComponent: () =>
          import('./pages/transactions/transactions.component').then(m => m.TransactionsComponent),
      },
      { path: '**', redirectTo: '' },
    ],
  },
];
