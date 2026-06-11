import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";

import { NzLayoutModule } from "ng-zorro-antd/layout";
import { NzMenuModule } from "ng-zorro-antd/menu";
import { NzIconModule } from "ng-zorro-antd/icon";
import { NzDrawerModule } from "ng-zorro-antd/drawer";
import { NzButtonModule } from "ng-zorro-antd/button";
import { NzInputNumberModule } from "ng-zorro-antd/input-number";
import { NzFormModule } from "ng-zorro-antd/form";
import { NzDividerModule } from "ng-zorro-antd/divider";
import { NzAlertModule } from "ng-zorro-antd/alert";
import { NzTagModule } from "ng-zorro-antd/tag";
import { NzSpinModule } from "ng-zorro-antd/spin";
import { NzTableModule } from "ng-zorro-antd/table";
import { NzCardModule } from "ng-zorro-antd/card";
import { NzInputModule } from "ng-zorro-antd/input";
import { NzGridModule } from "ng-zorro-antd/grid";

import { LayoutComponent } from "./layout/layout.component";
import { TradingDrawerComponent } from "./trading-drawer/trading-drawer.component";
import { StockDetailDrawerComponent } from "./stock-detail-drawer/stock-detail-drawer.component";

const NZ_MODULES = [
  NzLayoutModule,
  NzMenuModule,
  NzIconModule,
  NzDrawerModule,
  NzButtonModule,
  NzInputNumberModule,
  NzFormModule,
  NzDividerModule,
  NzAlertModule,
  NzTagModule,
  NzSpinModule,
  NzTableModule,
  NzCardModule,
  NzInputModule,
  NzGridModule,
];

@NgModule({
  declarations: [
    LayoutComponent,
    TradingDrawerComponent,
    StockDetailDrawerComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ...NZ_MODULES,
  ],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ...NZ_MODULES,
    LayoutComponent,
    TradingDrawerComponent,
    StockDetailDrawerComponent,
  ],
})
export class SharedModule {}
