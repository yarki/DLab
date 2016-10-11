import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { UserService } from '../user.service';

/**
 * This class represents the lazy loaded LoginComponent.
 */
@Component({
  moduleId: module.id,
  selector: 'sd-login',
  templateUrl: 'login.component.html',
  styleUrls: ['login.component.css'],
  providers: [UserService]
})
export class LoginComponent {

  constructor(private userService: UserService, private router: Router) {}

	onSubmit(username, password) {
    this.userService.login(username, password).subscribe((result) => {
      if (result) {
        this.router.navigate(['/dashboard']);
        return true;
      }

      return false;
    });

  }
}
