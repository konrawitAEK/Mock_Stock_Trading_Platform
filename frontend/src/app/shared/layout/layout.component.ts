import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [NgIf, RouterOutlet, RouterLink, RouterLinkActive, NzLayoutModule, NzMenuModule, NzIconModule],
  template: `
    <nz-layout style="min-height: 100vh;">
      <nz-sider nzCollapsible [(nzCollapsed)]="isCollapsed" [nzTrigger]="null" nzWidth="220px" nzTheme="dark">
        <div style="height:64px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:16px;font-weight:700;padding:0 16px;overflow:hidden;">
          <span *ngIf="!isCollapsed">📈 MockStock</span>
          <span *ngIf="isCollapsed">📈</span>
        </div>
        <ul nz-menu nzTheme="dark" nzMode="inline" [nzInlineCollapsed]="isCollapsed">
          <li nz-menu-item routerLink="/" routerLinkActive="ant-menu-item-selected" [routerLinkActiveOptions]="{exact:true}">
            <span nz-icon nzType="dashboard"></span>
            <span>Dashboard</span>
          </li>
          <li nz-menu-item routerLink="/stocks" routerLinkActive="ant-menu-item-selected">
            <span nz-icon nzType="stock"></span>
            <span>Stocks</span>
          </li>
          <li nz-menu-item routerLink="/transactions" routerLinkActive="ant-menu-item-selected">
            <span nz-icon nzType="history"></span>
            <span>Transactions</span>
          </li>
        </ul>
      </nz-sider>
      <nz-layout>
        <nz-header style="background:#fff;padding:0 16px;display:flex;align-items:center;border-bottom:1px solid #f0f0f0;">
          <span nz-icon [nzType]="isCollapsed ? 'menu-unfold' : 'menu-fold'"
            style="font-size:18px;cursor:pointer;"
            (click)="isCollapsed = !isCollapsed">
          </span>
        </nz-header>
        <nz-content style="margin:24px;overflow:auto;">
          <router-outlet />
        </nz-content>
      </nz-layout>
    </nz-layout>
  `,
})
export class LayoutComponent {
  isCollapsed = false;
}
