package dridco.jmx

import org.zkoss.zk.ui.Session
import org.zkoss.zk.ui.Sessions

abstract class User {
    def isAuthenticated: Boolean
}

case class AuthenticatedUser(username:String, authorities:Seq[String]) extends User {
    def isAuthenticated: Boolean = true
}

case class AnonymousUser() extends User {
    def isAuthenticated: Boolean = false
}

object UserManager {
	val USER_SESSION_ATTR_NAME = "jdash.userKey"
	val TOKEN_SESSION_ATTR_NAME = "jdash.tokenKey"
}

class UserManager {
    import UserManager._
    
    def isAuthenticated = getUser.isAuthenticated
    
    def getUser: User = {
        val session = Sessions.getCurrent(true)

        if (session != null && session.hasAttribute(USER_SESSION_ATTR_NAME)) {
        	session.getAttribute(USER_SESSION_ATTR_NAME).asInstanceOf[User]
        } else {
            new AnonymousUser
        }
    }
	
	def checkAccess(roles:Seq[String]) {
	    val user = getUser
	    
        for (accessName <- roles) {
            if (!isAllowed(user, accessName)) {
                throw new SecurityException("Access denied");
            }
        }
	}
	
	def isAllowed(user:User, role:String): Boolean = {
	    user.isAuthenticated &&
	    	user.isInstanceOf[AuthenticatedUser] &&
	    	user.asInstanceOf[AuthenticatedUser].authorities.contains(role)
	}
	
	def loginUser(username:String, password:String): String = {
	    if (username == "admin" && password == "1234") {
	        val userToken = "someTokenblabla"
	        val session = Sessions.getCurrent(true)
	        session.setAttribute(USER_SESSION_ATTR_NAME, new AuthenticatedUser(username, Array("ADMIN")))
	        session.setAttribute(TOKEN_SESSION_ATTR_NAME, userToken)
	        userToken
	    }
	    else {
	        throw new SecurityException("Invalid login credentials")
	    }
	}
}