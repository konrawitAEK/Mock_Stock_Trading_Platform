import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Stock, StockDetail } from '../models';

@Injectable({ providedIn: 'root' })
export class StockService {
  constructor(private api: ApiService) {}

  getAll(): Observable<Stock[]> {
    return this.api.post<Stock[]>('/stocks', {});
  }

  getBySymbol(symbol: string): Observable<StockDetail> {
    return this.api.post<StockDetail>('/stocks/detail', { symbol });
  }
}
