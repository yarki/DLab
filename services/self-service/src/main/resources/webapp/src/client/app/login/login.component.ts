import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthenticationService } from './../security/authentication.service';
import { UserProfileService } from "../security/userProfile.service";
import { LoginModel } from "./loginModel";

@Component({
  moduleId: module.id,
  selector: 'sd-login',
  templateUrl: 'login.component.html',
  styleUrls: ['./login.component.css'],
  providers: [AuthenticationService, UserProfileService]
})
export class LoginComponent {
  model = new LoginModel ('', '');
  error = '';
  constructor(private authenticationService: AuthenticationService, private userProfileService : UserProfileService, private router: Router) {}

	login() {
    this.authenticationService
      .login(this.model.username, this.model.password)
      .subscribe((result) => {
        if (result) {
          this.router.navigate(['/dashboard']);
          return true;
        } else {
          this.error = 'Username or password is incorrect';
        }

        return false;
    });
  }

  ngOnInit() {
    this.userProfileService.isLoggedIn().subscribe(result => {
      if (result)
        this.router.navigate(['/dashboard']);
    });
  }
}
