import React from 'react';
import LoginInfo from '../interfaces/LoginInfo.interface';
import UsersDataService from '../api/UsersDataService'; 
import { Formik, Form, Field, ErrorMessage } from 'formik'; 
import ILoginInfoProps from '../interfaces/ILoginInfoProps.interface';
import ILoginInfoState from '../interfaces/ILoginInfoState.interface';
import { getLoginId, setLoginId} from '../LoginLocalStorage';

class Login extends React.Component<ILoginInfoProps, ILoginInfoState>{

  constructor(props: ILoginInfoProps){
    super(props)

    this.state = {
      loginId: '0',
      loginData: {
        email: '',
        password: ''
      }
    }

    this.onSubmit = this.onSubmit.bind(this)
  }

  async onSubmit(values: LoginInfo){
    await UsersDataService.userLogin(values)
    .then(res =>{
      setLoginId(String(res.data));
    })
    .then(() => this.props.history.push('/users'))
    .then(() => window.location.reload(true))  
  }

  render(){
    let {email, password} = this.state.loginData;

    return(
      <div>
        <h2>Enter Login Info</h2>
        <div>
          <Formik
            initialValues = {{ email, password}}
            onSubmit = {this.onSubmit}
          >
            {
              (props) => (
                <Form>
                  <fieldset>
                    <label>Email: </label>
                    <Field type="text" name="email"/>
                  </fieldset>
                  <fieldset>
                    <label>Password: </label>
                    <Field type="text" name="password"/>
                  </fieldset>
                  <button type="submit">Send</button>
                </Form>
              )
            }
          </Formik>
        </div>
      </div>
    )
  }
}

export default Login; 