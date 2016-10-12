import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login.component';
import { AuthenticationService } from './../security/authentication.service';

@NgModule({
    imports: [CommonModule],
    declarations: [LoginComponent],
    exports: [LoginComponent],
    providers: [AuthenticationService]
})

export class LoginModule { }
