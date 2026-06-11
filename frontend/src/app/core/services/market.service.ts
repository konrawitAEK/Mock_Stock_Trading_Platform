import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Stock } from '../models';

@Injectable({ providedIn: 'root' })
export class MarketService {
  constructor(private api: ApiService) {}

  simulate(): Observable<Stock[]> {
    return this.api.post<Stock[]>('/market/simulate');
  }
}
