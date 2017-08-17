import java.lang.management.*
import net.bull.javamelody.*;
import net.bull.javamelody.internal.model.*;

def os = ManagementFactory.operatingSystemMXBean 
println """OPERATING SYSTEM: 
\tOS architecture = $os.arch 
\tOS name = $os.name 
\tOS version = $os.version 
\tOS processors = $os.availableProcessors 
""" 
 
def rt = ManagementFactory.runtimeMXBean 
println """RUNTIME: 
   \tRuntime name = $rt.name 
   \tRuntime spec name = $rt.specName 
   \tRuntime vendor = $rt.specVendor 
   \tRuntime spec version = $rt.specVersion 
   \tRuntime management spec version = $rt.managementSpecVersion 
   """ 

def mem = ManagementFactory.memoryMXBean 
def heapUsage = mem.heapMemoryUsage 
def nonHeapUsage = mem.nonHeapMemoryUsage 

println """MEMORY: 
   HEAP STORAGE: 
      \tMemory committed = $heapUsage.committed 
      \tMemory init = $heapUsage.init 
      \tMemory max = $heapUsage.max 
      \tMemory used = Math.round($heapUsage.used/ 1024 / 1024) + " Mb" NON-HEAP STORAGE: 
      \tNon-heap memory committed = $nonHeapUsage.committed 
      \tNon-heap memory init = $nonHeapUsage.init 
      \tNon-heap memory max = $nonHeapUsage.max 
      \tNon-heap memory used = $nonHeapUsage.used 
   """
  
println "GARBAGE COLLECTION:" 
ManagementFactory.garbageCollectorMXBeans.each { gc ->
   println "\tname = $gc.name"
   println "\t\tcollection count = $gc.collectionCount"
   println "\t\tcollection time = $gc.collectionTime"
   String[] mpoolNames =   gc.memoryPoolNames
	
   mpoolNames.each { 
      mpoolName -> println "\t\tmpool name = $mpoolName"
   } 
}
