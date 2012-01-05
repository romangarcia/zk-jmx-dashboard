package dridco.jmx
import org.scalatest.matchers.BePropertyMatchResult
import org.scalatest.matchers.BePropertyMatcher

trait TypeMatchers {
    def anInstanceOf[T](implicit manifest: Manifest[T]) = { 
	     val clazz = manifest.erasure.asInstanceOf[Class[T]] 
	     new BePropertyMatcher[AnyRef] { 
    	 	def apply(left: AnyRef) = BePropertyMatchResult(left.getClass.isAssignableFrom(clazz), "an instance of " + clazz.getName) 
         } 
    } 
}