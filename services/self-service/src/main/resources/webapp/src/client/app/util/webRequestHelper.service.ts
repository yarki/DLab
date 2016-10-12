import { Injectable } from '@angular/core';
import {Headers} from "@angular/http";

@Injectable()
export class WebRequestHelper {
  getJsonHeader() : Headers {
    let result = new Headers();
    result.append('Content-type', 'application/json; charset=utf-8');
    return result;
  }
}
