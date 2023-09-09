import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpHeaders } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AwsService {
  AUTH_SERVER: string = environment.api_url;
  httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        Authorization: 'Bearer ',
        observe: 'response'
      })
  };

  constructor(private httpClient: HttpClient) {

  }

  createProject(projectName:String){
    return this.httpClient.post(
      `${this.AUTH_SERVER}/api/createCustomModelProject`,
      projectName,
      { responseType: 'text'}
    ).pipe(catchError(this.handleError<any>('createProject error')));
  }

  deleteProject(projectId:string){
    return this.httpClient.post(
      `${this.AUTH_SERVER}/api/deleteCustomModelProject`,
      projectId,
      { responseType: 'text'}
    ).pipe(catchError(this.handleError<any>('deleteProject error')));
  }

  getProjectAll(){
    return this.httpClient.get(
      `${this.AUTH_SERVER}/api/getProjectAll`,
      this.httpOptions
    ).pipe(catchError(this.handleError<any>('test error')));
  }

  public TrainingDataSet(projectId:string, label:string, file:any): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('file', file);
    formdata.append('projectId', projectId);
    formdata.append('label', label);

    // append your data
    return this.httpClient.post(`${this.AUTH_SERVER}/api/trainingDataSet`, formdata);
  }

  TestDataSet(projectId:string, label:string, file:any): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('file', file);
    formdata.append('projectId', projectId);
    formdata.append('label', label);

    return this.httpClient.post(`${this.AUTH_SERVER}/api/testDataSet`, formdata);
  }

  CreateModel(projectId:string, versionName:string): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('projectId', projectId);
    formdata.append('versionName', versionName);

    return this.httpClient.post(`${this.AUTH_SERVER}/api/createModel`, formdata);
  }

  test(): Observable<any> {
    return this.httpClient.get(
      `${this.AUTH_SERVER}/api/test`,
      this.httpOptions
    ).pipe(catchError(this.handleError<any>('test error')));
  }

  getListLabel(projectId:string): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('projectId', projectId);

    return this.httpClient.post(`${this.AUTH_SERVER}/api/getListLabel`, formdata);
  }

  getTrainingData(projectId:string): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('projectId', projectId);

    return this.httpClient.post(`${this.AUTH_SERVER}/api/getTrainingData`, formdata);
  }

  getTestData(projectId:string): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('projectId', projectId);

    return this.httpClient.post(`${this.AUTH_SERVER}/api/getTestData`, formdata);
  }

  uploadS3(file:any): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('file', file);
    return this.httpClient.post(
      `${this.AUTH_SERVER}/api/uploadS3`,
      formdata,
      { responseType: 'text'}
    ).pipe(catchError(this.handleError<any>('uploadS3 error')));
  }

  getStatusCustomModel(projectId:string): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('projectId', projectId);
    return this.httpClient.post(`${this.AUTH_SERVER}/api/statusCustomModel`, formdata)
    .pipe(catchError(this.handleError<any>('statusCustomModel error')));
  }

  startCustomModel(projectId:string): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('projectId', projectId);
    return this.httpClient.post(`${this.AUTH_SERVER}/api/startCustomModel`, formdata)
    .pipe(catchError(this.handleError<any>('startCustomModel error')));
  }

  stopCustomModel(projectId:string): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('projectId', projectId);
    return this.httpClient.post(`${this.AUTH_SERVER}/api/stopCustomModel`, formdata)
    .pipe(catchError(this.handleError<any>('stopCustomModel error')));
  }

  customModel(projectId:string, file:any): Observable<any> {
    let formdata: FormData = new FormData();
    formdata.append('file', file);
    formdata.append('projectId', projectId);
    return this.httpClient.post(
      `${this.AUTH_SERVER}/api/customModel`,
      formdata,
      { responseType: 'text'}
    ).pipe(catchError(this.handleError<any>('customModel error')));
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error);
      console.log(`${operation} failed: ${error.message}`);

      return of(result as T);
    };
  }
}