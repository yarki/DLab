import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home.component';
import { AuthenticationService } from './../security/authentication.service';

import { EnvironmentsService } from './../services/environments.service';

import { ModalModule } from './../components/modal/index';
import { GridModule } from './../components/grid/index';

@NgModule({
  imports: [CommonModule, ModalModule, GridModule],
  declarations: [HomeComponent],
  exports: [HomeComponent],
  providers: [AuthenticationService, EnvironmentsService]
})
export class HomeModule { }
