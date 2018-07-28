package kvverti.enim.abiescript;

/**
 * A stack-based operator used in AbieScript custom functions.
 * On second thought I should have called it AbieFunctionOperatorThing.
 */
@FunctionalInterface
interface AbieFunctionOperator {

    void eval(Stack stack);

    default int arity() { return 1; }

    interface Nullary extends AbieFunctionOperator {

        @Override
        default int arity() { return 0; }
    }

    interface Unary extends AbieFunctionOperator {

        @Override
        default int arity() { return 1; }
    }


    interface Binary extends AbieFunctionOperator {

        @Override
        default int arity() { return 2; }
    }


    interface Ternary extends AbieFunctionOperator {

        @Override
        default int arity() { return 3; }
    }
}
