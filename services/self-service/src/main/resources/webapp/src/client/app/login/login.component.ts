import { Component } from '@angular/core';

import { AuthenticationService } from './../security/authentication.service';
import { UserProfileService } from "../security/userProfile.service";
import { LoginModel } from "./loginModel";
import {AppRoutingService} from "../routing/appRouting.service";

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
  constructor(private authenticationService: AuthenticationService, private userProfileService : UserProfileService, private appRoutingService : AppRoutingService) {}

	login() {
    this.authenticationService
      .login(this.model.username, this.model.password)
      .subscribe((result) => {
        if (result) {
          this.appRoutingService.redirectToHomePage();
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
        this.appRoutingService.redirectToHomePage();
    });
  }
}
