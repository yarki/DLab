import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HomeComponent } from './home.component';
import { AuthenticationService } from './../security/authentication.service';
import { EnvironmentsService } from './home.service';

import { ModalModule } from './../components/modal/index';
import { ResourcesModule } from './../components/list-of-resources/index';

@NgModule({
  imports: [CommonModule, ModalModule, ResourcesModule],
  declarations: [HomeComponent],
  exports: [HomeComponent],
  providers: [AuthenticationService, EnvironmentsService]
})
export class HomeModule { }
