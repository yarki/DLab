import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home.component';
import { AuthenticationService } from './../security/authentication.service';
import { ModalModule } from './../components/modal/index';

@NgModule({
  imports: [CommonModule, ModalModule],
  declarations: [HomeComponent],
  exports: [HomeComponent],
  providers: [AuthenticationService]
})
export class HomeModule { }
