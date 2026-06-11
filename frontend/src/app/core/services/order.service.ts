import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { OrderRequest, PortfolioResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private api: ApiService) {}

  buy(req: OrderRequest): Observable<PortfolioResponse> {
    return this.api.post<PortfolioResponse>('/orders/buy', req);
  }

  sell(req: OrderRequest): Observable<PortfolioResponse> {
    return this.api.post<PortfolioResponse>('/orders/sell', req);
  }
}
