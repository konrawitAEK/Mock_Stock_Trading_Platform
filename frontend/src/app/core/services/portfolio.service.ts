import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { PortfolioResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  constructor(private api: ApiService) {}

  get(): Observable<PortfolioResponse> {
    return this.api.get<PortfolioResponse>('/portfolio');
  }
}
