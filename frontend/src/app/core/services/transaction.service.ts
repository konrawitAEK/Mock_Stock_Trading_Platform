import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Transaction } from '../models';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  constructor(private api: ApiService) {}

  getAll(): Observable<Transaction[]> {
    return this.api.get<Transaction[]>('/transactions');
  }
}
