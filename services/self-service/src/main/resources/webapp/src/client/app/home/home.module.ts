import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home.component';
import { AuthenticationService } from './../security/authentication.service';

@NgModule({
  imports: [CommonModule],
  declarations: [HomeComponent],
  exports: [HomeComponent],
  providers: [AuthenticationService]
})
export class HomeModule { }
