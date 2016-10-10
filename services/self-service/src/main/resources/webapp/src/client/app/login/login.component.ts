import { Component } from '@angular/core';

/**
 * This class represents the lazy loaded LoginComponent.
 */
@Component({
  moduleId: module.id,
  selector: 'sd-login',
  templateUrl: 'login.component.html',
  styleUrls: ['login.component.css'],
})
export class LoginComponent {
	onLogin() {
		var username = (<HTMLInputElement>document.getElementById('username')).value;
		var password = (<HTMLInputElement>document.getElementById('password')).value;
		var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
      if (this.readyState == 4 && this.status == 200) {
        console.log(this.responseText);
      }
    };
    xhttp.open("POST", "/api/login", true);
    xhttp.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhttp.send(JSON.stringify({"username": username, "password": password,"access_token":""}));
		return false;
	}
}
