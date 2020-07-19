import React from 'react';
import { Link } from 'react-router-dom';

class Header extends React.Component{
  render(){
    return(
      <div>
        <Link to={'/login'}>Login</Link> 
        &nbsp;
        <button>Logout</button>
        <hr/>
      </div>
    )
  }
}

export default Header; 