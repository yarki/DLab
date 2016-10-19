import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import {Observable} from "rxjs";


@Injectable()
export class EnvironmentsService {
constructor(private http: Http) {
      console.log('EnvironmentsService created.');

  }

  getEnvironmentsList() : Observable<String>{
      return this.http
          .get('app/home/data.json')
          .map(res => res.json());
  }

}
