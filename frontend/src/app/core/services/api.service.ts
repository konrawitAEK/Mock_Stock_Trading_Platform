import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '@env/environment';
import { ApiResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  get<T>(path: string): Observable<T> {
    return this.http
      .get<ApiResponse<T>>(`${this.baseUrl}${path}`)
      .pipe(map(this.unwrap), catchError(this.handleError));
  }

  post<T>(path: string, body?: unknown): Observable<T> {
    return this.http
      .post<ApiResponse<T>>(`${this.baseUrl}${path}`, body ?? null)
      .pipe(map(this.unwrap), catchError(this.handleError));
  }

  private unwrap = <T>(res: ApiResponse<T>): T => {
    if (!res.success) throw new Error(res.message);
    return res.data;
  };

  private handleError = (err: HttpErrorResponse): Observable<never> => {
    const message =
      (err.error as ApiResponse<unknown>)?.message ??
      err.message ??
      'Network error';
    return throwError(() => new Error(message));
  };
}
