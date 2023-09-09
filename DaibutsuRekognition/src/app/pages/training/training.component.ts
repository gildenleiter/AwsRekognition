import { Component, OnInit } from '@angular/core';
import {AwsService} from '../../service/aws.service';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

interface USERS {
  id: Number;
  name: String;
  username: String;
  email: String;
}

@Component({
  selector: 'app-training',
  templateUrl: './training.component.html',
  styleUrls: ['./training.component.css']
})
export class TrainingComponent implements OnInit {

  files: any = null;
  cancelFlg: boolean = false;

  trainingData: any = null;
  testData: any = null;
  columnName = ['id'];

  constructor(
    private awsService: AwsService
  ) { }

  ngOnInit(): void {
    this.awsService.getProjectAll().subscribe(x => { 
      console.log('getProjectAll:', x);
      var listProjectElement = document.getElementById("listProject");

      if(listProjectElement != undefined) {
        for (let i = 0; i < x.length; i++) {
          // optionタグを作成する
          var option = document.createElement("option");
          option.text = x[i].id;
          option.value = x[i].id;
          // selectタグの子要素にoptionタグを追加する
          listProjectElement.appendChild(option);
        }
      }

      this.getListLabel();

    });
  }

  getListLabel(){
    var projectId = this.getProjectId();
    this.awsService.getListLabel(projectId).subscribe(x => {
    });
  }

  onChangeFileInput(event:any) {
    // 複数選択したファイルをFileList型オブジェクトとして取得
    let files = event.target.files;

    this.files = files;

    for (let file of files) {
      console.log(file); // [object file]...
    }
  }

  onS3Upload(){
    for(var i = 0; i < this.files.length; i++){
      var file = this.files[i];
      this.awsService.uploadS3(file).subscribe(x => { 
          console.log('Subscriber:', x);
          var element = document.getElementById("resultLog");
          if(element != undefined) {
            var resultLog =  element.innerHTML;
            element.innerHTML = resultLog + "\n" + x;
          }
        }
      );
    }
  }

  onTraining(){
    var projectId = this.getProjectId();
    var labelElement = document.getElementById("label") as HTMLInputElement;
    var label = labelElement.value;

    this.TrainingDataSet(projectId, label, this.files);
  }

  public async TrainingDataSet(projectId:string, label:string, files:any) {
    for(var i = 0; i < files.length; i++){
      var file = files[i];
        // 同期処理
        await this.awsService.TrainingDataSet(
          projectId,
          label,
          file
          // 引数
        ).toPromise()
        .then((response: any) => {
          // 正常系処理
          console.log('Subscriber:', response);
          var element = document.getElementById("resultLog");
          if(element != undefined) {
            var resultLog =  element.innerHTML;
            element.innerHTML = resultLog + "\n" + response;
          }
        })
        .catch((error: any) => {
          // 異常系処理
        });
    }
  }

  onTest(){
    var projectId = this.getProjectId();
    var labelElement = document.getElementById("label") as HTMLInputElement;
    var label = labelElement.value;

    this.TestDataSet(projectId, label, this.files);
  }

  public async TestDataSet(projectId:string, label:string, files:any) {
    for(var i = 0; i < files.length; i++){
      var file = files[i];
      // 同期処理
      await this.awsService.TestDataSet(
        projectId,
        label,
        file
        // 引数
      ).toPromise()
      .then((response: any) => {
        // 正常系処理
        console.log('Subscriber:', response);
        var element = document.getElementById("resultLog");
        if(element != undefined) {
          var resultLog =  element.innerHTML;
          element.innerHTML = resultLog + "\n" + response;
        }
      })
      .catch((error: any) => {
        // 異常系処理
      });
    }
  }

  onCancel(){
    this.cancelFlg = true;
  }

  getTrainingData(){
    var projectId = this.getProjectId();
    this.awsService.getTrainingData(projectId).toPromise()
      .then((response: any) => {
        // 正常系処理
        this.trainingData = [{id: '1'}];
        // console.log('Subscriber:', response);
        // var element = document.getElementById("resultLog");
        // if(element != undefined) {
        //   var resultLog =  element.innerHTML;
        //   element.innerHTML = resultLog + "\n" + response;
        // }
      })
      .catch((error: any) => {
        // 異常系処理
      });
  }

  getTestData(){
    var projectId = this.getProjectId();
    this.awsService.getTestData(projectId).toPromise()
      .then((response: any) => {
        // 正常系処理
        this.testData = [{id: '1'}];
        // console.log('Subscriber:', response);
        // var element = document.getElementById("resultLog");
        // if(element != undefined) {
        //   var resultLog =  element.innerHTML;
        //   element.innerHTML = resultLog + "\n" + response;
        // }
      })
      .catch((error: any) => {
        // 異常系処理
      });
  }

  getTestDataImage(obj:any){

  }

  deleteData(obj:any){
    
  }

  private getProjectId(){
    var listProjectIdElement = document.getElementById("listProject") as HTMLSelectElement;
    var projectId = "";

    if(listProjectIdElement.options.length > 0){
      projectId = listProjectIdElement.options[listProjectIdElement.selectedIndex].value;
    }

    return projectId;
  }


}
