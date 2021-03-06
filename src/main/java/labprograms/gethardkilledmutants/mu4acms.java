package labprograms.gethardkilledmutants;

import labprograms.ACMS.sourceCode.AirlinesBaggageBillingService;
import labprograms.constant.Constant;
import labprograms.method.Methods4Testing;
import labprograms.mutants.Mutant;
import labprograms.mutants.MutantsSet;
import labprograms.newStrategy.utl.InstantiationTestFrame;
import labprograms.strategies.util.Control;
import labprograms.strategies.util.MeasureRecorder;
import labprograms.strategies.util.TestCasesOfPartition;
import labprograms.strategies.util.TimeRecorder;
import labprograms.testCase.TestCase4ACMS;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class mu4acms {
    public static void main(String[] args) {
        mu4acms a = new mu4acms();
        a.testing("ACMS");
    }

    public void testing(String objectName) {
        //建立分区与测试用例之间的关系
        TestCasesOfPartition testCasesOfPartition = new TestCasesOfPartition(objectName);
        //实例化control类
        Control control = new Control(objectName);
        //记录时间的对象
        TimeRecorder timeRecorder = new TimeRecorder();
        //记录metrics值的对象
        MeasureRecorder measureRecorder = new MeasureRecorder();
        //获得变异体集合
        MutantsSet mutantsSet = new MutantsSet(objectName);
        Map<String, Mutant> mutantMap = mutantsSet.getMutants();
        //初始化一个存放杀死的变异体的集合
        Set<String> killedMutants = new HashSet<>();
        //获得待测程序的待测方法名
        String methodName = new Methods4Testing(objectName).getMethodName();

        //遍历变异体
        for (Map.Entry<String, Mutant> entry : mutantMap.entrySet()) {
            Mutant mutant = entry.getValue();
            String mutantname = entry.getKey();
            int killednum = 0;
            int partitionIndex = 0;

            for (int j = 0; j < 10000; j++) {
                //选择分区
                partitionIndex = new Random().nextInt(Constant.getPartitionNumber(objectName));
                //选择测试用例
                String testframesAndMr = testCasesOfPartition.
                        getSourceFollowAndMR(partitionIndex);
                Object mutantInstance = null;
                Method mutantMethod = null;
                Class mutantClazz = null;
                try {
                    mutantClazz = Class.forName(mutant.getFullName());
                    Constructor mutantConstructor = mutantClazz.getConstructor();
                    mutantInstance = mutantConstructor.newInstance();

                    double sourceResult = 0;
                    double expectedResult = 0;
                    mutantMethod = mutantClazz.getMethod(methodName, int.class, int.class,
                            boolean.class, double.class, double.class);

                    // 产生测试用例
                    Object stc = InstantiationTestFrame.instantiation(objectName,
                            testframesAndMr.split(";")[0]);
                    TestCase4ACMS sourceTestCase = (TestCase4ACMS) stc;


                    //　执行测试用例
                    sourceResult = (double) mutantMethod.invoke(mutantInstance,
                            sourceTestCase.getAirClass(), sourceTestCase.getArea(),
                            sourceTestCase.isStudent(), sourceTestCase.getLuggage(),
                            sourceTestCase.getEconomicfee());
                    expectedResult = new AirlinesBaggageBillingService().feeCalculation(sourceTestCase.getAirClass(), sourceTestCase.getArea(),
                            sourceTestCase.isStudent(), sourceTestCase.getLuggage(),
                            sourceTestCase.getEconomicfee());

                    if (sourceResult != expectedResult) {
                        //检测出故障
                        killednum++;
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (killednum <= 1000 && killednum != 0)
                System.out.println(mutantname + ";" + killednum);
        }
    }
}
