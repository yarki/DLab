import { Injectable } from '@angular/core';
import {Http} from '@angular/http';
import {Router} from "@angular/router";

@Injectable()
export class AppRoutingService {
  constructor(private http: Http, private router: Router) {
  }

  redirectToLoginPage()
  {
    if(this.router.url != "/login")
      this.router.navigate(['/login']);
  }

  redirectToHomePage()
  {
    this.router.navigate(['/dashboard']);
  }
}
